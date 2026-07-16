package com.badminton.service;

import com.badminton.vo.VenueSlotVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 场地时段服务接口
 */
public interface VenueSlotService {

    /**
     * 查询某日期所有场地的可约时段
     *
     * @param date 日期
     * @return 时段列表
     */
    List<VenueSlotVO> listSlotsByDate(LocalDate date);

    /**
     * 查询某场地某日期的时段
     *
     * @param venueId 场地ID
     * @param date    日期
     * @return 时段列表
     */
    List<VenueSlotVO> listSlotsByVenueAndDate(Long venueId, LocalDate date);

    /**
     * 为某场地生成某日期的时段
     *
     * @param venueId 场地ID
     * @param date    日期
     */
    void generateSlots(Long venueId, LocalDate date);

    /**
     * 为所有开放场地生成某日期的时段
     *
     * @param date 日期
     */
    void generateSlotsForAll(LocalDate date);
}
