package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.CoachCourseDTO;
import com.badminton.entity.Coach;
import com.badminton.entity.CoachCourse;
import com.badminton.mapper.CoachCourseMapper;
import com.badminton.mapper.CoachMapper;
import com.badminton.service.CoachCourseService;
import com.badminton.vo.CoachCourseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练课程服务实现
 */
@Service
@RequiredArgsConstructor
public class CoachCourseServiceImpl implements CoachCourseService {

    private final CoachCourseMapper coachCourseMapper;
    private final CoachMapper coachMapper;

    @Override
    public Long createCourse(CoachCourseDTO dto) {
        checkCoachExists(dto.getCoachId());
        CoachCourse course = new CoachCourse();
        BeanUtils.copyProperties(dto, course);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        coachCourseMapper.insert(course);
        return course.getId();
    }

    @Override
    public void updateCourse(CoachCourseDTO dto) {
        CoachCourse course = coachCourseMapper.selectById(dto.getId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        checkCoachExists(dto.getCoachId());
        BeanUtils.copyProperties(dto, course);
        course.setUpdateTime(LocalDateTime.now());
        coachCourseMapper.updateById(course);
    }

    @Override
    public void deleteCourse(Long id) {
        coachCourseMapper.deleteById(id);
    }

    @Override
    public CoachCourseVO getCourseById(Long id) {
        CoachCourse course = coachCourseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return convertToVO(course);
    }

    @Override
    public List<CoachCourseVO> listByCoachId(Long coachId) {
        LambdaQueryWrapper<CoachCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachCourse::getCoachId, coachId)
                .orderByDesc(CoachCourse::getCreateTime);
        return coachCourseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachCourseVO> listOpenCoursesByCoachId(Long coachId) {
        LambdaQueryWrapper<CoachCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachCourse::getCoachId, coachId)
                .eq(CoachCourse::getStatus, 1)
                .orderByDesc(CoachCourse::getCreateTime);
        return coachCourseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private void checkCoachExists(Long coachId) {
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null || coach.getDeleted() == 1) {
            throw new BusinessException("教练不存在");
        }
    }

    private CoachCourseVO convertToVO(CoachCourse course) {
        CoachCourseVO vo = new CoachCourseVO();
        BeanUtils.copyProperties(course, vo);
        Coach coach = coachMapper.selectById(course.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getName());
        }
        return vo;
    }
}
