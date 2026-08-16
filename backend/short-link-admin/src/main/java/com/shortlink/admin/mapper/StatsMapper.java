package com.shortlink.admin.mapper;

import com.shortlink.admin.pojo.vo.StatsTopVO;
import com.shortlink.admin.pojo.vo.StatsTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计查询 Mapper
 */
@Mapper
public interface StatsMapper {

    /** 短链总访问量 */
    @Select("SELECT IFNULL(SUM(visit_count), 0) FROM t_short_link")
    Long sumTotalVisits();

    /** 今日访问量 */
    @Select("SELECT COUNT(*) FROM t_short_link_log WHERE visit_time >= #{startTime}")
    Long countVisitsSince(@Param("startTime") LocalDateTime startTime);

    /** 按日访问趋势 */
    @Select("SELECT DATE_FORMAT(visit_time, '%Y-%m-%d') AS statDate, COUNT(*) AS visitCount "
            + "FROM t_short_link_log WHERE visit_time >= #{startTime} "
            + "GROUP BY statDate ORDER BY statDate")
    List<StatsTrendVO> selectTrend(@Param("startTime") LocalDateTime startTime);

    /** 访问量 TopN 短链 */
    @Select("SELECT short_code, original_url, visit_count FROM t_short_link "
            + "ORDER BY visit_count DESC LIMIT #{limit}")
    List<StatsTopVO> selectTop(@Param("limit") int limit);
}
