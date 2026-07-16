package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 场地时段 VO
 */
@Data
public class VenueSlotVO {

    private Long id;

    /**
     * 场地ID
     */
    private Long venueId;

    /**
     * 预约日期
     */
    private LocalDate bookingDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 售价
     */
    private BigDecimal price;

    /**
     * 状态：0关闭 1可约 2已约
     */
    private Integer status;
}
