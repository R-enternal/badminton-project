package com.badminton.vo;

import lombok.Data;

/**
 * 商品分类 VO
 */
@Data
public class ProductCategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
}
