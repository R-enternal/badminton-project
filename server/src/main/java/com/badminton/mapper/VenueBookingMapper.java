package com.badminton.mapper;

import com.badminton.entity.VenueBooking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 场地预约订单 Mapper
 */
@Mapper
public interface VenueBookingMapper extends BaseMapper<VenueBooking> {
}
