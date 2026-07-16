package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.VenueDTO;
import com.badminton.entity.Venue;
import com.badminton.mapper.VenueMapper;
import com.badminton.service.VenueService;
import com.badminton.vo.VenueVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场地服务实现
 */
@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueMapper venueMapper;

    @Override
    public List<VenueVO> listOpenVenues() {
        LambdaQueryWrapper<Venue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Venue::getStatus, 1)
                .eq(Venue::getDeleted, 0)
                .orderByAsc(Venue::getSortOrder)
                .orderByDesc(Venue::getCreateTime);
        List<Venue> venues = venueMapper.selectList(wrapper);
        return venues.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public VenueVO getVenueById(Long id) {
        Venue venue = venueMapper.selectById(id);
        if (venue == null || venue.getDeleted() == 1) {
            throw new BusinessException("场地不存在");
        }
        return convertToVO(venue);
    }

    @Override
    public Venue getEntityById(Long id) {
        return venueMapper.selectById(id);
    }

    @Override
    public Long createVenue(VenueDTO dto) {
        Venue venue = new Venue();
        BeanUtils.copyProperties(dto, venue);
        venue.setCreateTime(LocalDateTime.now());
        venue.setUpdateTime(LocalDateTime.now());
        venueMapper.insert(venue);
        return venue.getId();
    }

    @Override
    public void updateVenue(VenueDTO dto) {
        Venue venue = venueMapper.selectById(dto.getId());
        if (venue == null || venue.getDeleted() == 1) {
            throw new BusinessException("场地不存在");
        }
        BeanUtils.copyProperties(dto, venue);
        venue.setUpdateTime(LocalDateTime.now());
        venueMapper.updateById(venue);
    }

    @Override
    public void deleteVenue(Long id) {
        venueMapper.deleteById(id);
    }

    private VenueVO convertToVO(Venue venue) {
        VenueVO vo = new VenueVO();
        BeanUtils.copyProperties(venue, vo);
        return vo;
    }
}
