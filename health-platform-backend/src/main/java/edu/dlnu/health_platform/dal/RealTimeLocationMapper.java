package edu.dlnu.health_platform.dal;

import edu.dlnu.health_platform.entity.RealTimeLocation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * real_time_locations 表 Mapper（注解 SQL 风格）
 * 位置心跳：同一用户重复上报时覆盖更新（ON DUPLICATE KEY UPDATE）
 */
@Mapper
public interface RealTimeLocationMapper {

    //上报最新位置（存在则更新，不存在则插入）
    @Insert("INSERT INTO real_time_locations (user_id, longitude, latitude) " +
            "VALUES (#{userId}, #{longitude}, #{latitude}) " +
            "ON DUPLICATE KEY UPDATE longitude = #{longitude}, latitude = #{latitude}")
    int report(@Param("userId") Long userId,
               @Param("longitude") BigDecimal longitude,
               @Param("latitude") BigDecimal latitude);

    //查询某用户的最新位置
    @Select("SELECT * FROM real_time_locations WHERE user_id = #{userId}")
    RealTimeLocation findByUserId(@Param("userId") Long userId);
}
