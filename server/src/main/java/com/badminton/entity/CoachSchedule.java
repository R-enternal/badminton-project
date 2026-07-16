package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 教练排班实体
 */
@Data
@TableName("coach_schedule")
public class CoachSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教练ID
     */
    private Long coachId;

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
     * 关联课程ID，空表示通用可约
     */
    private Long courseId;

    /**
     * 是否被预约：0否 1是
     */
    private Integer isBooked;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
