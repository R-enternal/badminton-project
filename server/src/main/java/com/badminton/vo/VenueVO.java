package com.badminton.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 场地 VO
 */
@Data
public class VenueVO {

    private Long id;

    /**
     * 场地名称
     */
    private String name;

    /**
     * 位置描述
     */
    private String location;

    /**
     * 每小时单价
     */
    private BigDecimal pricePerHour;

    /**
     * 排序
     */
    private Integer sortOrder;
}
