package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体
 */
@Data
@TableName("product_sku")
public class ProductSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SPU_ID
     */
    private Long spuId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 规格组合 JSON
     */
    private String specs;

    /**
     * 规格哈希，数据库生成列，Java 不维护
     */
    @TableField(exist = false)
    private String specsHash;

    /**
     * 售价
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 状态：0下架 1上架
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
