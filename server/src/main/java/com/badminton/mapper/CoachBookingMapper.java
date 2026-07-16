package com.badminton.mapper;

import com.badminton.entity.CoachBooking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 私教课程预约订单 Mapper
 */
@Mapper
public interface CoachBookingMapper extends BaseMapper<CoachBooking> {
}
