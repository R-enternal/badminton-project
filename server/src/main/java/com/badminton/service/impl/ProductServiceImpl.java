package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.ProductSkuDTO;
import com.badminton.dto.ProductSpuDTO;
import com.badminton.entity.ProductCategory;
import com.badminton.entity.ProductSku;
import com.badminton.entity.ProductSpu;
import com.badminton.mapper.ProductCategoryMapper;
import com.badminton.mapper.ProductSkuMapper;
import com.badminton.mapper.ProductSpuMapper;
import com.badminton.service.ProductService;
import com.badminton.vo.ProductDetailVO;
import com.badminton.vo.ProductSkuVO;
import com.badminton.vo.ProductSpuVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductCategoryMapper productCategoryMapper;

    // ==================== SPU ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpu(ProductSpuDTO dto) {
        checkCategoryExists(dto.getCategoryId());
        ProductSpu spu = new ProductSpu();
        BeanUtils.copyProperties(dto, spu);
        spu.setCreateTime(LocalDateTime.now());
        spu.setUpdateTime(LocalDateTime.now());
        productSpuMapper.insert(spu);
        return spu.getId();
    }

    @Override
    public void updateSpu(ProductSpuDTO dto) {
        ProductSpu spu = productSpuMapper.selectById(dto.getId());
        if (spu == null || spu.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
        checkCategoryExists(dto.getCategoryId());
        BeanUtils.copyProperties(dto, spu);
        spu.setUpdateTime(LocalDateTime.now());
        productSpuMapper.updateById(spu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpu(Long id) {
        // 删除 SPU 同时删除 SKU
        productSpuMapper.deleteById(id);
        productSkuMapper.delete(
                new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, id)
        );
    }

    @Override
    public ProductSpuVO getSpuById(Long id) {
        ProductSpu spu = productSpuMapper.selectById(id);
        if (spu == null || spu.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
        return convertSpuToVO(spu);
    }

    @Override
    public List<ProductSpuVO> listSpus(Long categoryId, String keyword, Integer status) {
        LambdaQueryWrapper<ProductSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, ProductSpu::getCategoryId, categoryId)
                .eq(status != null, ProductSpu::getStatus, status)
                .eq(ProductSpu::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ProductSpu::getName, keyword);
        }
        wrapper.orderByDesc(ProductSpu::getCreateTime);
        return productSpuMapper.selectList(wrapper).stream()
                .map(this::convertSpuToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        ProductSpu spu = productSpuMapper.selectById(id);
        if (spu == null || spu.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
        ProductDetailVO detail = new ProductDetailVO();
        detail.setSpu(convertSpuToVO(spu));
        detail.setSkuList(listSkusBySpuId(id));
        return detail;
    }

    // ==================== SKU ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSku(ProductSkuDTO dto) {
        checkSpuExists(dto.getSpuId());
        ProductSku sku = new ProductSku();
        BeanUtils.copyProperties(dto, sku);
        sku.setCreateTime(LocalDateTime.now());
        sku.setUpdateTime(LocalDateTime.now());
        productSkuMapper.insert(sku);
        return sku.getId();
    }

    @Override
    public void updateSku(ProductSkuDTO dto) {
        ProductSku sku = productSkuMapper.selectById(dto.getId());
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        checkSpuExists(dto.getSpuId());
        BeanUtils.copyProperties(dto, sku);
        sku.setUpdateTime(LocalDateTime.now());
        productSkuMapper.updateById(sku);
    }

    @Override
    public void deleteSku(Long id) {
        productSkuMapper.deleteById(id);
    }

    @Override
    public ProductSkuVO getSkuById(Long id) {
        ProductSku sku = productSkuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        return convertSkuToVO(sku);
    }

    @Override
    public List<ProductSkuVO> listSkusBySpuId(Long spuId) {
        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSku::getSpuId, spuId)
                .orderByDesc(ProductSku::getCreateTime);
        return productSkuMapper.selectList(wrapper).stream()
                .map(this::convertSkuToVO)
                .collect(Collectors.toList());
    }

    private void checkCategoryExists(Long categoryId) {
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
    }

    private void checkSpuExists(Long spuId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null || spu.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
    }

    private ProductSpuVO convertSpuToVO(ProductSpu spu) {
        ProductSpuVO vo = new ProductSpuVO();
        BeanUtils.copyProperties(spu, vo);
        ProductCategory category = productCategoryMapper.selectById(spu.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }

    private ProductSkuVO convertSkuToVO(ProductSku sku) {
        ProductSkuVO vo = new ProductSkuVO();
        BeanUtils.copyProperties(sku, vo);
        return vo;
    }
}
