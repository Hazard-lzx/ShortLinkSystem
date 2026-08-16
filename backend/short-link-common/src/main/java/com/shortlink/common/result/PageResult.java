package com.shortlink.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页的数据记录列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页显示的记录数
     */
    private Long size;

    /**
     * 创建分页结果对象
     *
     * @param records 当前页的数据记录列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页显示的记录数
     * @param <T>     数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(total);
        pageResult.setCurrent(current);
        pageResult.setSize(size);
        return pageResult;
    }
}