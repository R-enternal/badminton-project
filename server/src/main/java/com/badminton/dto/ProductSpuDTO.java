package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品SPU DTO
 */
@Data
public class ProductSpuDTO {

    private Long id;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;

    private String mainImage;

    private String detail;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortOrder;
}
