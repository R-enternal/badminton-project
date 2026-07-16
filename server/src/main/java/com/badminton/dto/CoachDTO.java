package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 教练 DTO
 */
@Data
public class CoachDTO {

    private Long id;

    /**
     * 教练姓名
     */
    @NotBlank(message = "教练姓名不能为空")
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 个人介绍
     */
    private String intro;

    /**
     * 擅长领域
     */
    private String specialty;

    /**
     * 状态：0禁用 1启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
