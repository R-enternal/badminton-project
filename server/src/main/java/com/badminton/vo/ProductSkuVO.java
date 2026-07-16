package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品 SKU VO
 */
@Data
public class ProductSkuVO {

    private Long id;
    private Long spuId;
    private String skuCode;
    private String specs;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
}
