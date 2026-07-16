package com.badminton.service;

import com.badminton.dto.CoachScheduleDTO;
import com.badminton.vo.CoachScheduleVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 教练排班服务接口
 */
public interface CoachScheduleService {

    Long createSchedule(CoachScheduleDTO dto);

    void updateSchedule(CoachScheduleDTO dto);

    void deleteSchedule(Long id);

    List<CoachScheduleVO> listByCoachAndDate(Long coachId, LocalDate workDate);

    List<CoachScheduleVO> listAvailableByCoachAndDate(Long coachId, LocalDate workDate);
}
