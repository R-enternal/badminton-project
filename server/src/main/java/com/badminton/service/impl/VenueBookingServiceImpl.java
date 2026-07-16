package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.VenueBookingDTO;
import com.badminton.entity.Venue;
import com.badminton.entity.VenueBooking;
import com.badminton.entity.VenueSlot;
import com.badminton.mapper.VenueBookingMapper;
import com.badminton.mapper.VenueMapper;
import com.badminton.mapper.VenueSlotMapper;
import com.badminton.service.VenueBookingService;
import com.badminton.util.OrderNoUtil;
import com.badminton.vo.VenueBookingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 场地预约订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VenueBookingServiceImpl implements VenueBookingService {

    private final VenueBookingMapper venueBookingMapper;
    private final VenueSlotMapper venueSlotMapper;
    private final VenueMapper venueMapper;
    private final RedissonClient redissonClient;

    /**
     * 订单过期时间：15分钟
     */
    private static final int EXPIRE_MINUTES = 15;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueBookingVO prepareBooking(Long userId, VenueBookingDTO dto) {
        Long slotId = dto.getSlotId();
        String lockKey = "lock:venue:slot:" + slotId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            // 1. 尝试获取分布式锁，最多等待3秒，持有10秒（看门狗自动续期）
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("当前时段过于火爆，请刷新后重试");
            }

            // 2. 查询时段
            VenueSlot slot = venueSlotMapper.selectById(slotId);
            if (slot == null || slot.getStatus() != 1) {
                throw new BusinessException("该时段已被预约或已关闭");
            }

            // 3. 检查用户是否已预约该时段（包括待支付、已支付、已核销）
            Long existsCount = venueBookingMapper.selectCount(
                    new LambdaQueryWrapper<VenueBooking>()
                            .eq(VenueBooking::getSlotId, slotId)
                            .eq(VenueBooking::getUserId, userId)
                            .in(VenueBooking::getStatus, Arrays.asList(0, 1, 3))
            );
            if (existsCount > 0) {
                throw new BusinessException("您已预约该时段");
            }

            // 4. 乐观锁更新时段状态为已约
            int affected = venueSlotMapper.update(null,
                    new LambdaUpdateWrapper<VenueSlot>()
                            .eq(VenueSlot::getId, slotId)
                            .eq(VenueSlot::getStatus, 1)
                            .eq(VenueSlot::getVersion, slot.getVersion())
                            .set(VenueSlot::getStatus, 2)
                            .setSql("version = version + 1")
            );
            if (affected == 0) {
                throw new BusinessException("预约失败，该时段刚被他人预约");
            }

            // 5. 生成待支付订单
            VenueBooking booking = new VenueBooking();
            booking.setOrderNo(OrderNoUtil.generateVenueBookingNo());
            booking.setUserId(userId);
            booking.setVenueId(slot.getVenueId());
            booking.setSlotId(slotId);
            booking.setBookingDate(slot.getBookingDate());
            booking.setStartTime(slot.getStartTime());
            booking.setEndTime(slot.getEndTime());
            booking.setAmount(slot.getPrice());
            booking.setStatus(0);
            booking.setExpireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());
            venueBookingMapper.insert(booking);

            return convertToVO(booking);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            // 6. 只有当前线程持有锁时才释放
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long orderId) {
        VenueBooking booking = venueBookingMapper.selectById(orderId);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        // 只能取消待支付或已支付的订单
        if (booking.getStatus() != 0 && booking.getStatus() != 1) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 1. 更新订单状态为已取消
        booking.setStatus(2);
        booking.setUpdateTime(LocalDateTime.now());
        venueBookingMapper.updateById(booking);

        // 2. 释放时段
        venueSlotMapper.update(null,
                new LambdaUpdateWrapper<VenueSlot>()
                        .eq(VenueSlot::getId, booking.getSlotId())
                        .set(VenueSlot::getStatus, 1)
                        .setSql("version = version + 1")
        );
    }

    @Override
    public List<VenueBookingVO> listMyBookings(Long userId, Integer status) {
        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueBooking::getUserId, userId);
        if (status != null) {
            wrapper.eq(VenueBooking::getStatus, status);
        }
        wrapper.orderByDesc(VenueBooking::getCreateTime);
        List<VenueBooking> bookings = venueBookingMapper.selectList(wrapper);
        return bookings.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public VenueBookingVO getBookingDetail(Long userId, Long orderId) {
        VenueBooking booking = venueBookingMapper.selectById(orderId);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        return convertToVO(booking);
    }

    private VenueBookingVO convertToVO(VenueBooking booking) {
        VenueBookingVO vo = new VenueBookingVO();
        BeanUtils.copyProperties(booking, vo);

        // 查询场地名称
        Venue venue = venueMapper.selectById(booking.getVenueId());
        if (venue != null) {
            vo.setVenueName(venue.getName());
        }
        return vo;
    }
}
