package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体
 */
@Data
@TableName("cart_item")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * SKU_ID
     */
    private Long skuId;

    /**
     * SPU_ID
     */
    private Long spuId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中：0否 1是
     */
    private Integer selected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
