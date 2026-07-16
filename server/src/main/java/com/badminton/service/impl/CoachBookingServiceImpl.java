package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.CoachBookingDTO;
import com.badminton.entity.Coach;
import com.badminton.entity.CoachBooking;
import com.badminton.entity.CoachCourse;
import com.badminton.entity.CoachSchedule;
import com.badminton.mapper.CoachBookingMapper;
import com.badminton.mapper.CoachMapper;
import com.badminton.mapper.CoachCourseMapper;
import com.badminton.mapper.CoachScheduleMapper;
import com.badminton.service.CoachBookingService;
import com.badminton.util.OrderNoUtil;
import com.badminton.vo.CoachBookingVO;
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
 * 私教课程预约订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachBookingServiceImpl implements CoachBookingService {

    private final CoachBookingMapper coachBookingMapper;
    private final CoachScheduleMapper coachScheduleMapper;
    private final CoachCourseMapper coachCourseMapper;
    private final CoachMapper coachMapper;
    private final RedissonClient redissonClient;

    private static final int EXPIRE_MINUTES = 15;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachBookingVO prepareBooking(Long userId, CoachBookingDTO dto) {
        Long scheduleId = dto.getScheduleId();
        Long courseId = dto.getCourseId();

        String lockKey = "lock:coach:schedule:" + scheduleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("当前时段过于火爆，请刷新后重试");
            }

            // 1. 查询排班
            CoachSchedule schedule = coachScheduleMapper.selectById(scheduleId);
            if (schedule == null || schedule.getIsBooked() == 1) {
                throw new BusinessException("该时段已被预约或不存在");
            }

            // 2. 查询课程
            CoachCourse course = coachCourseMapper.selectById(courseId);
            if (course == null || course.getStatus() != 1) {
                throw new BusinessException("课程不存在或已下架");
            }

            // 3. 判断课程时长是否超出排班区间
            LocalDateTime scheduleStart = schedule.getWorkDate().atTime(schedule.getStartTime());
            LocalDateTime scheduleEnd = schedule.getWorkDate().atTime(schedule.getEndTime());
            LocalDateTime courseEnd = scheduleStart.plusMinutes(course.getDurationMinutes());
            if (courseEnd.isAfter(scheduleEnd)) {
                throw new BusinessException("课程时长超出该排班可用时间");
            }

            // 4. 检查用户是否已预约该排班
            Long existsCount = coachBookingMapper.selectCount(
                    new LambdaQueryWrapper<CoachBooking>()
                            .eq(CoachBooking::getScheduleId, scheduleId)
                            .eq(CoachBooking::getUserId, userId)
                            .in(CoachBooking::getStatus, Arrays.asList(0, 1, 3))
            );
            if (existsCount > 0) {
                throw new BusinessException("您已预约该时段");
            }

            // 5. 乐观锁更新排班为已约
            int affected = coachScheduleMapper.update(null,
                    new LambdaUpdateWrapper<CoachSchedule>()
                            .eq(CoachSchedule::getId, scheduleId)
                            .eq(CoachSchedule::getIsBooked, 0)
                            .eq(CoachSchedule::getVersion, schedule.getVersion())
                            .set(CoachSchedule::getIsBooked, 1)
                            .setSql("version = version + 1")
            );
            if (affected == 0) {
                throw new BusinessException("预约失败，该时段刚被他人预约");
            }

            // 6. 生成待支付订单
            CoachBooking booking = new CoachBooking();
            booking.setOrderNo(OrderNoUtil.generateCoachBookingNo());
            booking.setUserId(userId);
            booking.setCoachId(schedule.getCoachId());
            booking.setScheduleId(scheduleId);
            booking.setCourseId(courseId);
            booking.setWorkDate(schedule.getWorkDate());
            booking.setStartTime(schedule.getStartTime());
            // 根据课程时长计算实际结束时间
            booking.setEndTime(schedule.getStartTime().plusMinutes(course.getDurationMinutes()));
            booking.setAmount(course.getPrice());
            booking.setStatus(0);
            booking.setExpireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());
            coachBookingMapper.insert(booking);

            return convertToVO(booking);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long orderId) {
        CoachBooking booking = coachBookingMapper.selectById(orderId);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (booking.getStatus() != 0 && booking.getStatus() != 1) {
            throw new BusinessException("订单状态不允许取消");
        }

        booking.setStatus(2);
        booking.setUpdateTime(LocalDateTime.now());
        coachBookingMapper.updateById(booking);

        coachScheduleMapper.update(null,
                new LambdaUpdateWrapper<CoachSchedule>()
                        .eq(CoachSchedule::getId, booking.getScheduleId())
                        .set(CoachSchedule::getIsBooked, 0)
                        .setSql("version = version + 1")
        );
    }

    @Override
    public List<CoachBookingVO> listMyBookings(Long userId, Integer status) {
        LambdaQueryWrapper<CoachBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachBooking::getUserId, userId);
        if (status != null) {
            wrapper.eq(CoachBooking::getStatus, status);
        }
        wrapper.orderByDesc(CoachBooking::getCreateTime);
        return coachBookingMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CoachBookingVO getBookingDetail(Long userId, Long orderId) {
        CoachBooking booking = coachBookingMapper.selectById(orderId);
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        return convertToVO(booking);
    }

    private CoachBookingVO convertToVO(CoachBooking booking) {
        CoachBookingVO vo = new CoachBookingVO();
        BeanUtils.copyProperties(booking, vo);
        Coach coach = coachMapper.selectById(booking.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getName());
        }
        CoachCourse course = coachCourseMapper.selectById(booking.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        return vo;
    }
}
