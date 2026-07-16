package com.badminton.service;

import com.badminton.dto.CoachBookingDTO;
import com.badminton.vo.CoachBookingVO;

import java.util.List;

/**
 * 私教课程预约订单服务接口
 */
public interface CoachBookingService {

    /**
     * 预约私教课程
     */
    CoachBookingVO prepareBooking(Long userId, CoachBookingDTO dto);

    /**
     * 取消预约
     */
    void cancelBooking(Long userId, Long orderId);

    /**
     * 我的课程预约列表
     */
    List<CoachBookingVO> listMyBookings(Long userId, Integer status);

    /**
     * 预约详情
     */
    CoachBookingVO getBookingDetail(Long userId, Long orderId);
}
