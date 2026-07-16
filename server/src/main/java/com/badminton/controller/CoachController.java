package com.badminton.controller;

import com.badminton.common.Result;
import com.badminton.annotation.RequireRole;
import com.badminton.dto.CoachBookingDTO;
import com.badminton.dto.CoachDTO;
import com.badminton.dto.CoachCourseDTO;
import com.badminton.dto.CoachScheduleDTO;
import com.badminton.service.CoachBookingService;
import com.badminton.service.CoachCourseService;
import com.badminton.service.CoachScheduleService;
import com.badminton.service.CoachService;
import com.badminton.util.UserContext;
import com.badminton.vo.CoachBookingVO;
import com.badminton.vo.CoachCourseVO;
import com.badminton.vo.CoachScheduleVO;
import com.badminton.vo.CoachVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 教练课程模块接口
 */
@Tag(name = "教练课程模块", description = "教练、课程、排班、课程预约相关接口")
@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;
    private final CoachCourseService coachCourseService;
    private final CoachScheduleService coachScheduleService;
    private final CoachBookingService coachBookingService;

    // ==================== 教练管理 ====================

    @Operation(summary = "新增教练")
    @RequireRole("ADMIN")
    @PostMapping
    public Result<Long> createCoach(@Valid @RequestBody CoachDTO dto) {
        return Result.success(coachService.createCoach(dto));
    }

    @Operation(summary = "修改教练")
    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public Result<Void> updateCoach(@PathVariable Long id, @Valid @RequestBody CoachDTO dto) {
        dto.setId(id);
        coachService.updateCoach(dto);
        return Result.success();
    }

    @Operation(summary = "删除教练")
    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCoach(@PathVariable Long id) {
        coachService.deleteCoach(id);
        return Result.success();
    }

    @Operation(summary = "教练详情")
    @GetMapping("/detail/{id}")
    public Result<CoachVO> getCoach(@PathVariable Long id) {
        return Result.success(coachService.getCoachById(id));
    }

    @Operation(summary = "教练列表", description = "管理后台使用，包含禁用状态")
    @RequireRole("ADMIN")
    @GetMapping("/list")
    public Result<List<CoachVO>> listCoaches() {
        return Result.success(coachService.listAllCoaches());
    }

    @Operation(summary = "启用教练列表", description = "C端展示使用")
    @GetMapping
    public Result<List<CoachVO>> listOpenCoaches() {
        return Result.success(coachService.listOpenCoaches());
    }

    // ==================== 课程管理 ====================

    @Operation(summary = "新增课程")
    @RequireRole("ADMIN")
    @PostMapping("/course")
    public Result<Long> createCourse(@Valid @RequestBody CoachCourseDTO dto) {
        return Result.success(coachCourseService.createCourse(dto));
    }

    @Operation(summary = "修改课程")
    @RequireRole("ADMIN")
    @PutMapping("/course/{id}")
    public Result<Void> updateCourse(@PathVariable Long id, @Valid @RequestBody CoachCourseDTO dto) {
        dto.setId(id);
        coachCourseService.updateCourse(dto);
        return Result.success();
    }

    @Operation(summary = "删除课程")
    @RequireRole("ADMIN")
    @DeleteMapping("/course/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        coachCourseService.deleteCourse(id);
        return Result.success();
    }

    @Operation(summary = "课程详情")
    @GetMapping("/course/{id}")
    public Result<CoachCourseVO> getCourse(@PathVariable Long id) {
        return Result.success(coachCourseService.getCourseById(id));
    }

    @Operation(summary = "教练课程列表")
    @GetMapping("/{coachId}/courses")
    public Result<List<CoachCourseVO>> listCourses(@PathVariable Long coachId) {
        return Result.success(coachCourseService.listByCoachId(coachId));
    }

    // ==================== 排班管理 ====================

    @Operation(summary = "新增排班")
    @RequireRole("ADMIN")
    @PostMapping("/schedule")
    public Result<Long> createSchedule(@Valid @RequestBody CoachScheduleDTO dto) {
        return Result.success(coachScheduleService.createSchedule(dto));
    }

    @Operation(summary = "修改排班")
    @RequireRole("ADMIN")
    @PutMapping("/schedule/{id}")
    public Result<Void> updateSchedule(@PathVariable Long id, @Valid @RequestBody CoachScheduleDTO dto) {
        dto.setId(id);
        coachScheduleService.updateSchedule(dto);
        return Result.success();
    }

    @Operation(summary = "删除排班")
    @RequireRole("ADMIN")
    @DeleteMapping("/schedule/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        coachScheduleService.deleteSchedule(id);
        return Result.success();
    }

    @Operation(summary = "查询排班", description = "管理后台查询全部排班，C端查询可约排班")
    @GetMapping("/schedule")
    public Result<List<CoachScheduleVO>> listSchedule(
            @RequestParam Long coachId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable) {
        if (onlyAvailable) {
            return Result.success(coachScheduleService.listAvailableByCoachAndDate(coachId, date));
        }
        return Result.success(coachScheduleService.listByCoachAndDate(coachId, date));
    }

    // ==================== 课程预约 ====================

    @Operation(summary = "预约私教课程")
    @PostMapping("/book")
    public Result<CoachBookingVO> book(@Valid @RequestBody CoachBookingDTO dto) {
        return Result.success(coachBookingService.prepareBooking(UserContext.getUserId(), dto));
    }

    @Operation(summary = "取消课程预约")
    @PostMapping("/book/cancel/{id}")
    public Result<Void> cancelBook(@PathVariable Long id) {
        coachBookingService.cancelBooking(UserContext.getUserId(), id);
        return Result.success();
    }

    @Operation(summary = "我的课程预约")
    @GetMapping("/book/my")
    public Result<List<CoachBookingVO>> myBooks(@RequestParam(required = false) Integer status) {
        return Result.success(coachBookingService.listMyBookings(UserContext.getUserId(), status));
    }
}
