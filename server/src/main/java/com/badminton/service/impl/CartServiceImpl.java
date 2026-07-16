package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.CartItemDTO;
import com.badminton.entity.CartItem;
import com.badminton.entity.ProductSku;
import com.badminton.entity.ProductSpu;
import com.badminton.mapper.CartItemMapper;
import com.badminton.mapper.ProductSkuMapper;
import com.badminton.mapper.ProductSpuMapper;
import com.badminton.service.CartService;
import com.badminton.vo.CartItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSpuMapper productSpuMapper;

    @Override
    public CartItemVO addToCart(Long userId, CartItemDTO dto) {
        ProductSku sku = productSkuMapper.selectById(dto.getSkuId());
        if (sku == null || sku.getStatus() != 1) {
            throw new BusinessException("商品规格不存在或已下架");
        }
        ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
        if (spu == null || spu.getDeleted() == 1 || spu.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (dto.getQuantity() > sku.getStock()) {
            throw new BusinessException("库存不足");
        }

        // 查询是否已存在
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, dto.getSkuId());
        CartItem cartItem = cartItemMapper.selectOne(wrapper);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + dto.getQuantity());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(cartItem);
        } else {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setSkuId(dto.getSkuId());
            cartItem.setSpuId(sku.getSpuId());
            cartItem.setQuantity(dto.getQuantity());
            cartItem.setSelected(1);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(cartItem);
        }

        return convertToVO(cartItem);
    }

    @Override
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        ProductSku sku = productSkuMapper.selectById(cartItem.getSkuId());
        if (quantity > sku.getStock()) {
            throw new BusinessException("库存不足");
        }
        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);
    }

    @Override
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    public void selectCartItem(Long userId, Long cartItemId, Integer selected) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartItem.setSelected(selected);
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);
    }

    @Override
    public List<CartItemVO> listCartItems(Long userId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime);
        return cartItemMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CartItemVO> listSelectedCartItems(Long userId, List<Long> cartItemIds) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
                .in(CartItem::getId, cartItemIds)
                .eq(CartItem::getSelected, 1);
        return cartItemMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CartItemVO convertToVO(CartItem cartItem) {
        CartItemVO vo = new CartItemVO();
        BeanUtils.copyProperties(cartItem, vo);
        ProductSku sku = productSkuMapper.selectById(cartItem.getSkuId());
        ProductSpu spu = productSpuMapper.selectById(cartItem.getSpuId());
        if (sku != null) {
            vo.setPrice(sku.getPrice());
            vo.setSkuSpecs(sku.getSpecs());
            vo.setTotalAmount(sku.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        if (spu != null) {
            vo.setSpuName(spu.getName());
            vo.setSkuImage(spu.getMainImage());
        }
        return vo;
    }
}
