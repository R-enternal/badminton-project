package com.badminton.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成工具
 */
public class OrderNoUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成场地预约订单号
     * 格式：SV + 时间戳 + 4位随机数
     */
    public static String generateVenueBookingNo() {
        return "SV" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + random4();
    }

    /**
     * 生成私教课程订单号
     */
    public static String generateCoachBookingNo() {
        return "SC" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + random4();
    }

    /**
     * 生成商城订单号
     */
    public static String generateShopOrderNo() {
        return "SO" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + random4();
    }

    private static String random4() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
