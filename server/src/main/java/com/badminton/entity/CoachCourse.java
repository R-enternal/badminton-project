package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教练课程实体
 */
@Data
@TableName("coach_course")
public class CoachCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 分类：成人/青少年/1对1/团体
     */
    private String category;

    /**
     * 单次时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 单次价格
     */
    private BigDecimal price;

    /**
     * 课程说明
     */
    private String description;

    /**
     * 状态：0下架 1上架
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
