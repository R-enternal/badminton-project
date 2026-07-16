package com.badminton.service;

import com.badminton.dto.CoachCourseDTO;
import com.badminton.vo.CoachCourseVO;

import java.util.List;

/**
 * 教练课程服务接口
 */
public interface CoachCourseService {

    Long createCourse(CoachCourseDTO dto);

    void updateCourse(CoachCourseDTO dto);

    void deleteCourse(Long id);

    CoachCourseVO getCourseById(Long id);

    List<CoachCourseVO> listByCoachId(Long coachId);

    List<CoachCourseVO> listOpenCoursesByCoachId(Long coachId);
}
