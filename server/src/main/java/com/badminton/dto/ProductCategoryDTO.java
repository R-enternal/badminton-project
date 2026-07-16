package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品分类 DTO
 */
@Data
public class ProductCategoryDTO {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId;

    private Integer sortOrder;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
