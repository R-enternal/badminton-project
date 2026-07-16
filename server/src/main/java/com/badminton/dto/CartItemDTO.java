package com.badminton.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车 DTO
 */
@Data
public class CartItemDTO {

    @NotNull(message = "SKU_ID不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
