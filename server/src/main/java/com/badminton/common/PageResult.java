package com.badminton.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应体
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    public static <T> PageResult<T> of(Long page, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setRecords(records);
        result.setPages(total == 0 ? 0 : (total + size - 1) / size);
        return result;
    }
}
