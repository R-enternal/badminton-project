package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.CoachDTO;
import com.badminton.entity.Coach;
import com.badminton.mapper.CoachMapper;
import com.badminton.service.CoachService;
import com.badminton.vo.CoachVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练服务实现
 */
@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachMapper coachMapper;

    @Override
    public Long createCoach(CoachDTO dto) {
        Coach coach = new Coach();
        BeanUtils.copyProperties(dto, coach);
        coach.setCreateTime(LocalDateTime.now());
        coach.setUpdateTime(LocalDateTime.now());
        coachMapper.insert(coach);
        return coach.getId();
    }

    @Override
    public void updateCoach(CoachDTO dto) {
        Coach coach = coachMapper.selectById(dto.getId());
        if (coach == null || coach.getDeleted() == 1) {
            throw new BusinessException("教练不存在");
        }
        BeanUtils.copyProperties(dto, coach);
        coach.setUpdateTime(LocalDateTime.now());
        coachMapper.updateById(coach);
    }

    @Override
    public void deleteCoach(Long id) {
        coachMapper.deleteById(id);
    }

    @Override
    public CoachVO getCoachById(Long id) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null || coach.getDeleted() == 1) {
            throw new BusinessException("教练不存在");
        }
        return convertToVO(coach);
    }

    @Override
    public List<CoachVO> listOpenCoaches() {
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coach::getStatus, 1)
                .eq(Coach::getDeleted, 0)
                .orderByAsc(Coach::getSortOrder)
                .orderByDesc(Coach::getCreateTime);
        return coachMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachVO> listAllCoaches() {
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coach::getDeleted, 0)
                .orderByAsc(Coach::getSortOrder)
                .orderByDesc(Coach::getCreateTime);
        return coachMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CoachVO convertToVO(Coach coach) {
        CoachVO vo = new CoachVO();
        BeanUtils.copyProperties(coach, vo);
        return vo;
    }
}
