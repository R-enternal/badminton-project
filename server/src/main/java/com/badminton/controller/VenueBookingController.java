package com.badminton.controller;

import com.badminton.common.Result;
import com.badminton.dto.VenueBookingDTO;
import com.badminton.service.VenueBookingService;
import com.badminton.util.UserContext;
import com.badminton.vo.VenueBookingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 场地预约订单相关接口
 */
@Tag(name = "场地预约模块", description = "场地预约订单相关接口")
@RestController
@RequestMapping("/api/venue/booking")
@RequiredArgsConstructor
public class VenueBookingController {

    private final VenueBookingService venueBookingService;

    /**
     * 预约场地
     */
    @Operation(summary = "预约场地", description = "选择时段生成待支付订单")
    @PostMapping("/prepare")
    public Result<VenueBookingVO> prepare(@Valid @RequestBody VenueBookingDTO dto) {
        return Result.success(venueBookingService.prepareBooking(UserContext.getUserId(), dto));
    }

    /**
     * 取消预约
     */
    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        venueBookingService.cancelBooking(UserContext.getUserId(), id);
        return Result.success();
    }

    /**
     * 我的预约列表
     */
    @Operation(summary = "我的预约列表")
    @GetMapping("/my")
    public Result<List<VenueBookingVO>> my(
            @RequestParam(required = false) Integer status) {
        return Result.success(venueBookingService.listMyBookings(UserContext.getUserId(), status));
    }

    /**
     * 预约详情
     */
    @Operation(summary = "预约详情")
    @GetMapping("/{id}")
    public Result<VenueBookingVO> detail(@PathVariable Long id) {
        return Result.success(venueBookingService.getBookingDetail(UserContext.getUserId(), id));
    }
}
