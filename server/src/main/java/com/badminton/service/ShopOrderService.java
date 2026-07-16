package com.badminton.service;

import com.badminton.dto.OrderCreateDTO;
import com.badminton.vo.ShopOrderVO;

import java.util.List;

/**
 * 商城订单服务接口
 */
public interface ShopOrderService {

    /**
     * 创建订单
     */
    ShopOrderVO createOrder(Long userId, OrderCreateDTO dto);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * 发货
     */
    void shipOrder(Long orderId, String logisticsNo);

    /**
     * 确认收货
     */
    void receiveOrder(Long userId, Long orderId);

    /**
     * 我的订单列表
     */
    List<ShopOrderVO> listMyOrders(Long userId, Integer status);

    /**
     * 订单详情
     */
    ShopOrderVO getOrderDetail(Long userId, Long orderId);

    /**
     * 管理后台订单列表
     */
    List<ShopOrderVO> listAllOrders(Integer status);
}
