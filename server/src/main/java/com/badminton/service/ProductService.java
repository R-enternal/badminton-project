package com.badminton.service;

import com.badminton.dto.ProductSkuDTO;
import com.badminton.dto.ProductSpuDTO;
import com.badminton.vo.ProductDetailVO;
import com.badminton.vo.ProductSkuVO;
import com.badminton.vo.ProductSpuVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    // SPU
    Long createSpu(ProductSpuDTO dto);

    void updateSpu(ProductSpuDTO dto);

    void deleteSpu(Long id);

    ProductSpuVO getSpuById(Long id);

    List<ProductSpuVO> listSpus(Long categoryId, String keyword, Integer status);

    ProductDetailVO getProductDetail(Long id);

    // SKU
    Long createSku(ProductSkuDTO dto);

    void updateSku(ProductSkuDTO dto);

    void deleteSku(Long id);

    ProductSkuVO getSkuById(Long id);

    List<ProductSkuVO> listSkusBySpuId(Long spuId);
}
