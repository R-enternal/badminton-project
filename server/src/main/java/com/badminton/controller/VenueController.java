package com.badminton.controller;

import com.badminton.common.Result;
import com.badminton.dto.VenueDTO;
import com.badminton.service.VenueService;
import com.badminton.service.VenueSlotService;
import com.badminton.vo.VenueSlotVO;
import com.badminton.vo.VenueVO;
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
 * 场地相关接口
 */
@Tag(name = "场地模块", description = "场地预约相关接口")
@RestController
@RequestMapping("/api/venue")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final VenueSlotService venueSlotService;

    @Operation(summary = "场地列表", description = "查询所有开放的场地")
    @GetMapping("/list")
    public Result<List<VenueVO>> list() {
        return Result.success(venueService.listOpenVenues());
    }

    @Operation(summary = "场地详情")
    @GetMapping("/detail/{id}")
    public Result<VenueVO> detail(@PathVariable Long id) {
        return Result.success(venueService.getVenueById(id));
    }

    @Operation(summary = "新增场地")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody VenueDTO dto) {
        return Result.success(venueService.createVenue(dto));
    }

    @Operation(summary = "修改场地")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody VenueDTO dto) {
        dto.setId(id);
        venueService.updateVenue(dto);
        return Result.success();
    }

    @Operation(summary = "删除场地")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return Result.success();
    }

    @Operation(summary = "查询场地时段")
    @GetMapping("/slots")
    public Result<List<VenueSlotVO>> slots(
            @RequestParam(required = false) Long venueId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (venueId == null) {
            return Result.success(venueSlotService.listSlotsByDate(date));
        }
        return Result.success(venueSlotService.listSlotsByVenueAndDate(venueId, date));
    }

    @Operation(summary = "生成场地时段")
    @PostMapping("/slots/generate")
    public Result<Void> generateSlots(
            @RequestParam(required = false) Long venueId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (venueId == null) {
            venueSlotService.generateSlotsForAll(date);
        } else {
            venueSlotService.generateSlots(venueId, date);
        }
        return Result.success();
    }
}
