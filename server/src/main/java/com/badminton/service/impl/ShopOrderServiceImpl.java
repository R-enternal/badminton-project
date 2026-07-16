package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.OrderCreateDTO;
import com.badminton.entity.CartItem;
import com.badminton.entity.ProductSku;
import com.badminton.entity.ShopOrder;
import com.badminton.entity.ShopOrderItem;
import com.badminton.mapper.CartItemMapper;
import com.badminton.mapper.ProductSkuMapper;
import com.badminton.mapper.ShopOrderItemMapper;
import com.badminton.mapper.ShopOrderMapper;
import com.badminton.service.CartService;
import com.badminton.service.ShopOrderService;
import com.badminton.util.OrderNoUtil;
import com.badminton.vo.CartItemVO;
import com.badminton.vo.ShopOrderItemVO;
import com.badminton.vo.ShopOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商城订单服务实现
 */
@Service
@RequiredArgsConstructor
public class ShopOrderServiceImpl implements ShopOrderService {

    private final ShopOrderMapper shopOrderMapper;
    private final ShopOrderItemMapper shopOrderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final CartService cartService;

    private static final int EXPIRE_MINUTES = 15;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopOrderVO createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 查询选中的购物车项
        List<CartItemVO> cartItems = cartService.listSelectedCartItems(userId, dto.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车项不存在或未选中");
        }

        // 2. 校验库存并计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVO cartItem : cartItems) {
            ProductSku sku = productSkuMapper.selectById(cartItem.getSkuId());
            if (sku == null || sku.getStatus() != 1) {
                throw new BusinessException("商品规格不存在或已下架");
            }
            if (cartItem.getQuantity() > sku.getStock()) {
                throw new BusinessException("商品「" + cartItem.getSpuName() + "」库存不足");
            }
            totalAmount = totalAmount.add(cartItem.getTotalAmount());
        }

        // 3. 创建订单
        ShopOrder order = new ShopOrder();
        order.setOrderNo(OrderNoUtil.generateShopOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount); // 暂不考虑运费和优惠
        order.setFreightAmount(BigDecimal.ZERO);
        order.setStatus(0);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        order.setExpireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        shopOrderMapper.insert(order);

        // 4. 创建订单明细并扣减库存
        for (CartItemVO cartItem : cartItems) {
            ProductSku sku = productSkuMapper.selectById(cartItem.getSkuId());

            ShopOrderItem item = new ShopOrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setSkuId(cartItem.getSkuId());
            item.setSpuId(cartItem.getSpuId());
            item.setSpuName(cartItem.getSpuName());
            item.setSkuSpecs(cartItem.getSkuSpecs());
            item.setSkuImage(cartItem.getSkuImage());
            item.setPrice(cartItem.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setTotalAmount(cartItem.getTotalAmount());
            item.setCreateTime(LocalDateTime.now());
            shopOrderItemMapper.insert(item);

            // 扣减库存
            sku.setStock(sku.getStock() - cartItem.getQuantity());
            sku.setUpdateTime(LocalDateTime.now());
            productSkuMapper.updateById(sku);

            // 删除购物车项
            cartItemMapper.deleteById(cartItem.getId());
        }

        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 恢复库存
        restoreStock(orderId);

        order.setStatus(5);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        shopOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, String logisticsNo) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不允许发货");
        }
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        shopOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long userId, Long orderId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许确认收货");
        }
        order.setStatus(4);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        shopOrderMapper.updateById(order);
    }

    @Override
    public List<ShopOrderVO> listMyOrders(Long userId, Integer status) {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrder::getUserId, userId);
        if (status != null) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        return shopOrderMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ShopOrderVO getOrderDetail(Long userId, Long orderId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        return convertToVO(order);
    }

    @Override
    public List<ShopOrderVO> listAllOrders(Integer status) {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        return shopOrderMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private void restoreStock(Long orderId) {
        List<ShopOrderItem> items = shopOrderItemMapper.selectList(
                new LambdaQueryWrapper<ShopOrderItem>().eq(ShopOrderItem::getOrderId, orderId)
        );
        for (ShopOrderItem item : items) {
            ProductSku sku = productSkuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                sku.setUpdateTime(LocalDateTime.now());
                productSkuMapper.updateById(sku);
            }
        }
    }

    private ShopOrderVO convertToVO(ShopOrder order) {
        ShopOrderVO vo = new ShopOrderVO();
        BeanUtils.copyProperties(order, vo);
        List<ShopOrderItem> items = shopOrderItemMapper.selectList(
                new LambdaQueryWrapper<ShopOrderItem>().eq(ShopOrderItem::getOrderId, order.getId())
        );
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    private ShopOrderItemVO convertItemToVO(ShopOrderItem item) {
        ShopOrderItemVO vo = new ShopOrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }
}
