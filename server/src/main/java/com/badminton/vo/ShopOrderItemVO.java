package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商城订单明细 VO
 */
@Data
public class ShopOrderItemVO {

    private Long id;
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String skuSpecs;
    private String skuImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}
