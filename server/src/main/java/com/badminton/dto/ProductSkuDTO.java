package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品SKU DTO
 */
@Data
public class ProductSkuDTO {

    private Long id;

    @NotNull(message = "SPU_ID不能为空")
    private Long spuId;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    /**
     * 规格组合 JSON 字符串，如 {"颜色":"红色","尺码":"L"}
     */
    @NotBlank(message = "规格不能为空")
    private String specs;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
