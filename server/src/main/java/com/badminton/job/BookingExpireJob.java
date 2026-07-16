package com.badminton.job;

import com.badminton.entity.*;
import com.badminton.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约订单过期处理定时任务
 * 每1分钟扫描一次待支付且已过期的订单，自动取消并释放时段
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpireJob {

    private final VenueBookingMapper venueBookingMapper;
    private final VenueSlotMapper venueSlotMapper;
    private final CoachBookingMapper coachBookingMapper;
    private final CoachScheduleMapper coachScheduleMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final ShopOrderItemMapper shopOrderItemMapper;
    private final ProductSkuMapper productSkuMapper;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void handleExpireBookings() {
        handleVenueBookingExpire();
        handleCoachBookingExpire();
        handleShopOrderExpire();
    }

    private void handleShopOrderExpire() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrder::getStatus, 0)
                .le(ShopOrder::getExpireTime, now);
        List<ShopOrder> expireOrders = shopOrderMapper.selectList(wrapper);

        if (expireOrders.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 条过期待支付商城订单", expireOrders.size());

        for (ShopOrder order : expireOrders) {
            try {
                // 恢复库存
                List<ShopOrderItem> items = shopOrderItemMapper.selectList(
                        new LambdaQueryWrapper<ShopOrderItem>().eq(ShopOrderItem::getOrderId, order.getId())
                );
                for (ShopOrderItem item : items) {
                    ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                    if (sku != null) {
                        sku.setStock(sku.getStock() + item.getQuantity());
                        sku.setUpdateTime(now);
                        productSkuMapper.updateById(sku);
                    }
                }

                shopOrderMapper.update(null,
                        new LambdaUpdateWrapper<ShopOrder>()
                                .eq(ShopOrder::getId, order.getId())
                                .set(ShopOrder::getStatus, 5)
                                .set(ShopOrder::getCancelTime, now)
                                .set(ShopOrder::getUpdateTime, now)
                );

                log.info("已自动取消商城过期订单：{}", order.getOrderNo());
            } catch (Exception e) {
                log.error("处理商城过期订单失败：{}，错误：{}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    private void handleVenueBookingExpire() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueBooking::getStatus, 0)
                .le(VenueBooking::getExpireTime, now);
        List<VenueBooking> expireBookings = venueBookingMapper.selectList(wrapper);

        if (expireBookings.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 条过期待支付场地预约订单", expireBookings.size());

        for (VenueBooking booking : expireBookings) {
            try {
                venueBookingMapper.update(null,
                        new LambdaUpdateWrapper<VenueBooking>()
                                .eq(VenueBooking::getId, booking.getId())
                                .set(VenueBooking::getStatus, 2)
                                .set(VenueBooking::getUpdateTime, now)
                );

                venueSlotMapper.update(null,
                        new LambdaUpdateWrapper<VenueSlot>()
                                .eq(VenueSlot::getId, booking.getSlotId())
                                .set(VenueSlot::getStatus, 1)
                                .setSql("version = version + 1")
                );

                log.info("已自动取消场地过期订单：{}，释放时段：{}", booking.getOrderNo(), booking.getSlotId());
            } catch (Exception e) {
                log.error("处理场地过期订单失败：{}，错误：{}", booking.getOrderNo(), e.getMessage());
            }
        }
    }

    private void handleCoachBookingExpire() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CoachBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachBooking::getStatus, 0)
                .le(CoachBooking::getExpireTime, now);
        List<CoachBooking> expireBookings = coachBookingMapper.selectList(wrapper);

        if (expireBookings.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 条过期待支付私教课程订单", expireBookings.size());

        for (CoachBooking booking : expireBookings) {
            try {
                coachBookingMapper.update(null,
                        new LambdaUpdateWrapper<CoachBooking>()
                                .eq(CoachBooking::getId, booking.getId())
                                .set(CoachBooking::getStatus, 2)
                                .set(CoachBooking::getUpdateTime, now)
                );

                coachScheduleMapper.update(null,
                        new LambdaUpdateWrapper<CoachSchedule>()
                                .eq(CoachSchedule::getId, booking.getScheduleId())
                                .set(CoachSchedule::getIsBooked, 0)
                                .setSql("version = version + 1")
                );

                log.info("已自动取消私教过期订单：{}，释放排班：{}", booking.getOrderNo(), booking.getScheduleId());
            } catch (Exception e) {
                log.error("处理私教过期订单失败：{}，错误：{}", booking.getOrderNo(), e.getMessage());
            }
        }
    }
}
