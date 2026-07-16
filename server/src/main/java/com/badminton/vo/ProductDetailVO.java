package com.badminton.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品详情 VO
 */
@Data
public class ProductDetailVO {

    private ProductSpuVO spu;
    private List<ProductSkuVO> skuList;
}
