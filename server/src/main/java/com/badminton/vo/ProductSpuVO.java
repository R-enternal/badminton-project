package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品 SPU VO
 */
@Data
public class ProductSpuVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private String detail;
    private Integer status;
    private Integer sortOrder;
}
