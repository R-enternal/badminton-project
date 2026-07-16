package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 场地预约订单 VO
 */
@Data
public class VenueBookingVO {

    private Long id;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 场地ID
     */
    private Long venueId;

    /**
     * 场地名称
     */
    private String venueName;

    /**
     * 时段ID
     */
    private Long slotId;

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
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 状态：0待支付 1已支付 2已取消 3已核销
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
