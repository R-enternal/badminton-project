package com.badminton.service;

import com.badminton.dto.CoachDTO;
import com.badminton.vo.CoachVO;

import java.util.List;

/**
 * 教练服务接口
 */
public interface CoachService {

    /**
     * 新增教练
     */
    Long createCoach(CoachDTO dto);

    /**
     * 修改教练
     */
    void updateCoach(CoachDTO dto);

    /**
     * 删除教练
     */
    void deleteCoach(Long id);

    /**
     * 查询教练详情
     */
    CoachVO getCoachById(Long id);

    /**
     * 查询启用的教练列表
     */
    List<CoachVO> listOpenCoaches();

    /**
     * 查询所有教练（管理后台）
     */
    List<CoachVO> listAllCoaches();
}
