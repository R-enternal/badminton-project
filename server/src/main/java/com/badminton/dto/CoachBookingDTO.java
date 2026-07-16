package com.badminton.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 私教课程预约请求参数
 */
@Data
public class CoachBookingDTO {

    /**
     * 排班ID
     */
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
