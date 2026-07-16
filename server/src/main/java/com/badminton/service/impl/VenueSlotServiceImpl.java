package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.entity.Venue;
import com.badminton.entity.VenueSlot;
import com.badminton.mapper.VenueMapper;
import com.badminton.mapper.VenueSlotMapper;
import com.badminton.service.VenueSlotService;
import com.badminton.vo.VenueSlotVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场地时段服务实现
 * 营业时间固定为 08:00-22:00，每小时一个时段
 */
@Service
@RequiredArgsConstructor
public class VenueSlotServiceImpl implements VenueSlotService {

    private final VenueSlotMapper venueSlotMapper;
    private final VenueMapper venueMapper;

    /**
     * 营业开始时间
     */
    private static final int OPEN_HOUR = 8;

    /**
     * 营业结束时间
     */
    private static final int CLOSE_HOUR = 22;

    @Override
    public List<VenueSlotVO> listSlotsByDate(LocalDate date) {
        LambdaQueryWrapper<VenueSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueSlot::getBookingDate, date)
                .orderByAsc(VenueSlot::getVenueId)
                .orderByAsc(VenueSlot::getStartTime);
        List<VenueSlot> slots = venueSlotMapper.selectList(wrapper);
        return slots.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueSlotVO> listSlotsByVenueAndDate(Long venueId, LocalDate date) {
        LambdaQueryWrapper<VenueSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueSlot::getVenueId, venueId)
                .eq(VenueSlot::getBookingDate, date)
                .orderByAsc(VenueSlot::getStartTime);
        List<VenueSlot> slots = venueSlotMapper.selectList(wrapper);
        return slots.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSlots(Long venueId, LocalDate date) {
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null || venue.getDeleted() == 1) {
            throw new BusinessException("场地不存在");
        }

        List<VenueSlot> slots = buildSlots(venueId, date, venue.getPricePerHour());
        for (VenueSlot slot : slots) {
            // 按唯一索引判断，已存在则跳过
            Long count = venueSlotMapper.selectCount(
                    new LambdaQueryWrapper<VenueSlot>()
                            .eq(VenueSlot::getVenueId, slot.getVenueId())
                            .eq(VenueSlot::getBookingDate, slot.getBookingDate())
                            .eq(VenueSlot::getStartTime, slot.getStartTime())
            );
            if (count == 0) {
                venueSlotMapper.insert(slot);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSlotsForAll(LocalDate date) {
        LambdaQueryWrapper<Venue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Venue::getStatus, 1).eq(Venue::getDeleted, 0);
        List<Venue> venues = venueMapper.selectList(wrapper);
        for (Venue venue : venues) {
            generateSlots(venue.getId(), date);
        }
    }

    /**
     * 构建某场地某日期的所有时段
     */
    private List<VenueSlot> buildSlots(Long venueId, LocalDate date, BigDecimal pricePerHour) {
        List<VenueSlot> slots = new ArrayList<>();
        for (int hour = OPEN_HOUR; hour < CLOSE_HOUR; hour++) {
            VenueSlot slot = new VenueSlot();
            slot.setVenueId(venueId);
            slot.setBookingDate(date);
            slot.setStartTime(LocalTime.of(hour, 0));
            slot.setEndTime(LocalTime.of(hour + 1, 0));
            slot.setPrice(pricePerHour);
            slot.setStatus(1);
            slot.setVersion(0);
            slots.add(slot);
        }
        return slots;
    }

    private VenueSlotVO convertToVO(VenueSlot slot) {
        VenueSlotVO vo = new VenueSlotVO();
        BeanUtils.copyProperties(slot, vo);
        return vo;
    }
}
