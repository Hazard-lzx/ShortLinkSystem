package com.shortlink.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.api.pojo.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    /** 游标分页查询，用于启动时全量加载布隆过滤器 */
    @Select("SELECT id, short_code FROM t_short_link WHERE id > #{lastId} ORDER BY id LIMIT #{size}")
    List<ShortLink> selectCursorAfter(@Param("lastId") Long lastId, @Param("size") int size);
}
