package com.badminton.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 场地预约请求参数
 */
@Data
public class VenueBookingDTO {

    /**
     * 时段ID
     */
    @NotNull(message = "时段ID不能为空")
    private Long slotId;
}
