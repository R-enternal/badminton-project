package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 教练课程 DTO
 */
@Data
public class CoachCourseDTO {

    private Long id;

    /**
     * 教练ID
     */
    @NotNull(message = "教练ID不能为空")
    private Long coachId;

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    private String name;

    /**
     * 分类
     */
    @NotBlank(message = "课程分类不能为空")
    private String category;

    /**
     * 单次时长（分钟）
     */
    @NotNull(message = "时长不能为空")
    private Integer durationMinutes;

    /**
     * 单次价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 课程说明
     */
    private String description;

    /**
     * 状态：0下架 1上架
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
