package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 场地 DTO
 */
@Data
public class VenueDTO {

    private Long id;

    @NotBlank(message = "场地名称不能为空")
    private String name;

    private String location;

    @NotNull(message = "单价不能为空")
    private BigDecimal pricePerHour;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortOrder;
}
