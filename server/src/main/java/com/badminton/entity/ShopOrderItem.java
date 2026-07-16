package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商城订单明细实体
 */
@Data
@TableName("shop_order_item")
public class ShopOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * SKU_ID
     */
    private Long skuId;

    /**
     * SPU_ID
     */
    private Long spuId;

    /**
     * 商品名称冗余
     */
    private String spuName;

    /**
     * 规格冗余 JSON
     */
    private String skuSpecs;

    /**
     * SKU图片冗余
     */
    private String skuImage;

    /**
     * 下单时单价
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计
     */
    private BigDecimal totalAmount;

    private LocalDateTime createTime;
}
