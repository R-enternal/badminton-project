package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 教练课程 VO
 */
@Data
public class CoachCourseVO {

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
     * 课程名称
     */
    private String name;

    /**
     * 分类
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
}
