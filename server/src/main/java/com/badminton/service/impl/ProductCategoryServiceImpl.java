package com.badminton.service.impl;

import com.badminton.dto.ProductCategoryDTO;
import com.badminton.entity.ProductCategory;
import com.badminton.mapper.ProductCategoryMapper;
import com.badminton.service.ProductCategoryService;
import com.badminton.vo.ProductCategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public Long createCategory(ProductCategoryDTO dto) {
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        productCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(ProductCategoryDTO dto) {
        ProductCategory category = productCategoryMapper.selectById(dto.getId());
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        BeanUtils.copyProperties(dto, category);
        category.setUpdateTime(LocalDateTime.now());
        productCategoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        productCategoryMapper.deleteById(id);
    }

    @Override
    public List<ProductCategoryVO> listAllCategories() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProductCategory::getSortOrder)
                .orderByDesc(ProductCategory::getCreateTime);
        return productCategoryMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCategoryVO> listOpenCategories() {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortOrder);
        return productCategoryMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private ProductCategoryVO convertToVO(ProductCategory category) {
        ProductCategoryVO vo = new ProductCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
