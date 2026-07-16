package com.badminton.service;

import com.badminton.dto.VenueDTO;
import com.badminton.entity.Venue;
import com.badminton.vo.VenueVO;

import java.util.List;

/**
 * 场地服务接口
 */
public interface VenueService {

    List<VenueVO> listOpenVenues();

    VenueVO getVenueById(Long id);

    Venue getEntityById(Long id);

    Long createVenue(VenueDTO dto);

    void updateVenue(VenueDTO dto);

    void deleteVenue(Long id);
}
