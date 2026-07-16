package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项 VO
 */
@Data
public class CartItemVO {

    private Long id;
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String skuSpecs;
    private String skuImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private BigDecimal totalAmount;
}
