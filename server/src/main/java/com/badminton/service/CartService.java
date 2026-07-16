package com.badminton.service;

import com.badminton.dto.CartItemDTO;
import com.badminton.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 加入购物车
     */
    CartItemVO addToCart(Long userId, CartItemDTO dto);

    /**
     * 修改购物车数量
     */
    void updateQuantity(Long userId, Long cartItemId, Integer quantity);

    /**
     * 删除购物车项
     */
    void deleteCartItem(Long userId, Long cartItemId);

    /**
     * 选中/取消选中
     */
    void selectCartItem(Long userId, Long cartItemId, Integer selected);

    /**
     * 查询购物车列表
     */
    List<CartItemVO> listCartItems(Long userId);

    /**
     * 查询选中的购物车项
     */
    List<CartItemVO> listSelectedCartItems(Long userId, List<Long> cartItemIds);
}
