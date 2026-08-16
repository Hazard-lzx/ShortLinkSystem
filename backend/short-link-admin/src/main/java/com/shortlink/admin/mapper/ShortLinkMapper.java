package com.shortlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.admin.pojo.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    /** 批量累计访问次数（Kafka 消费端调用） */
    @Update("UPDATE t_short_link SET visit_count = visit_count + #{count} WHERE short_code = #{shortCode}")
    int incrVisitCount(@Param("shortCode") String shortCode, @Param("count") long count);
}
