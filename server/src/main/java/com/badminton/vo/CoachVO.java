package com.badminton.vo;

import lombok.Data;

/**
 * 教练 VO
 */
@Data
public class CoachVO {

    private Long id;

    /**
     * 教练姓名
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 个人介绍
     */
    private String intro;

    /**
     * 擅长领域
     */
    private String specialty;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
