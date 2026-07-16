package com.badminton.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 教练排班 VO
 */
@Data
public class CoachScheduleVO {

    private Long id;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练姓名
     */
    private String coachName;

    /**
     * 工作日期
     */
    private LocalDate workDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 关联课程ID
     */
    private Long courseId;

    /**
     * 关联课程名称
     */
    private String courseName;

    /**
     * 是否被预约：0否 1是
     */
    private Integer isBooked;
}
