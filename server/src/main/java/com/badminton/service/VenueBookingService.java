package com.badminton.service;

import com.badminton.dto.VenueBookingDTO;
import com.badminton.vo.VenueBookingVO;

import java.util.List;

/**
 * 场地预约订单服务接口
 */
public interface VenueBookingService {

    /**
     * 预约场地（生成待支付订单）
     *
     * @param userId 用户ID
     * @param dto    预约参数
     * @return 预约订单
     */
    VenueBookingVO prepareBooking(Long userId, VenueBookingDTO dto);

    /**
     * 取消预约
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     */
    void cancelBooking(Long userId, Long orderId);

    /**
     * 查询我的预约列表
     *
     * @param userId 用户ID
     * @param status 状态，可选
     * @return 预约列表
     */
    List<VenueBookingVO> listMyBookings(Long userId, Integer status);

    /**
     * 查询预约详情
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 预约详情
     */
    VenueBookingVO getBookingDetail(Long userId, Long orderId);
}
