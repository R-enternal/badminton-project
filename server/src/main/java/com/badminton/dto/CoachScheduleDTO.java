package com.badminton.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 教练排班 DTO
 */
@Data
public class CoachScheduleDTO {

    private Long id;

    /**
     * 教练ID
     */
    @NotNull(message = "教练ID不能为空")
    private Long coachId;

    /**
     * 工作日期
     */
    @NotNull(message = "工作日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    /**
     * 关联课程ID
     */
    private Long courseId;
}
