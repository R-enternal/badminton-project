package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.CoachScheduleDTO;
import com.badminton.entity.Coach;
import com.badminton.entity.CoachCourse;
import com.badminton.entity.CoachSchedule;
import com.badminton.mapper.CoachMapper;
import com.badminton.mapper.CoachCourseMapper;
import com.badminton.mapper.CoachScheduleMapper;
import com.badminton.service.CoachScheduleService;
import com.badminton.vo.CoachScheduleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练排班服务实现
 */
@Service
@RequiredArgsConstructor
public class CoachScheduleServiceImpl implements CoachScheduleService {

    private final CoachScheduleMapper coachScheduleMapper;
    private final CoachMapper coachMapper;
    private final CoachCourseMapper coachCourseMapper;

    @Override
    public Long createSchedule(CoachScheduleDTO dto) {
        checkCoachExists(dto.getCoachId());
        // 时间校验
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        // 检查同一时间段是否已存在排班
        checkTimeConflict(dto.getCoachId(), dto.getWorkDate(), dto.getStartTime(), dto.getEndTime(), null);

        CoachSchedule schedule = new CoachSchedule();
        BeanUtils.copyProperties(dto, schedule);
        schedule.setIsBooked(0);
        schedule.setVersion(0);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());
        coachScheduleMapper.insert(schedule);
        return schedule.getId();
    }

    @Override
    public void updateSchedule(CoachScheduleDTO dto) {
        CoachSchedule schedule = coachScheduleMapper.selectById(dto.getId());
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        if (schedule.getIsBooked() == 1) {
            throw new BusinessException("该排班已被预约，不能修改");
        }
        checkCoachExists(dto.getCoachId());
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        checkTimeConflict(dto.getCoachId(), dto.getWorkDate(), dto.getStartTime(), dto.getEndTime(), dto.getId());

        BeanUtils.copyProperties(dto, schedule);
        schedule.setUpdateTime(LocalDateTime.now());
        coachScheduleMapper.updateById(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        CoachSchedule schedule = coachScheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        if (schedule.getIsBooked() == 1) {
            throw new BusinessException("该排班已被预约，不能删除");
        }
        coachScheduleMapper.deleteById(id);
    }

    @Override
    public List<CoachScheduleVO> listByCoachAndDate(Long coachId, LocalDate workDate) {
        LambdaQueryWrapper<CoachSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(coachId != null, CoachSchedule::getCoachId, coachId)
                .eq(workDate != null, CoachSchedule::getWorkDate, workDate)
                .orderByAsc(CoachSchedule::getStartTime);
        return coachScheduleMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachScheduleVO> listAvailableByCoachAndDate(Long coachId, LocalDate workDate) {
        LambdaQueryWrapper<CoachSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachSchedule::getCoachId, coachId)
                .eq(CoachSchedule::getWorkDate, workDate)
                .eq(CoachSchedule::getIsBooked, 0)
                .orderByAsc(CoachSchedule::getStartTime);
        return coachScheduleMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 检查时间冲突
     */
    private void checkTimeConflict(Long coachId, LocalDate workDate, java.time.LocalTime startTime,
                                   java.time.LocalTime endTime, Long excludeId) {
        LambdaQueryWrapper<CoachSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachSchedule::getCoachId, coachId)
                .eq(CoachSchedule::getWorkDate, workDate)
                // 时间段重叠条件：新开始 < 旧结束 且 新结束 > 旧开始
                .lt(CoachSchedule::getStartTime, endTime)
                .gt(CoachSchedule::getEndTime, startTime);
        if (excludeId != null) {
            wrapper.ne(CoachSchedule::getId, excludeId);
        }
        Long count = coachScheduleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该时间段与已有排班冲突");
        }
    }

    private void checkCoachExists(Long coachId) {
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null || coach.getDeleted() == 1) {
            throw new BusinessException("教练不存在");
        }
    }

    private CoachScheduleVO convertToVO(CoachSchedule schedule) {
        CoachScheduleVO vo = new CoachScheduleVO();
        BeanUtils.copyProperties(schedule, vo);
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getName());
        }
        if (schedule.getCourseId() != null) {
            CoachCourse course = coachCourseMapper.selectById(schedule.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
            }
        }
        return vo;
    }
}
