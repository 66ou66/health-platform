package edu.dlnu.health_platform.dal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.entity.VolunteerProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Map;

/**
 * volunteer_profiles 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface VolunteerProfileMapper {

    //根据用户ID查询档案
    @Select("SELECT * FROM volunteer_profiles WHERE user_id = #{userId}")
    VolunteerProfile findByUserId(@Param("userId") Long userId);

    //注册时创建空档案
    @Insert("INSERT INTO volunteer_profiles (user_id) VALUES (#{userId})")
    int insert(@Param("userId") Long userId);

    //更新档案（更新后重新进入待审核状态）
    @Update("UPDATE volunteer_profiles SET id_card_no = #{idCardNo}, skill_tags = #{skillTags}, " +
            "service_region = #{serviceRegion}, duty_area = #{dutyArea}, cert_status = 0, certify_reason = NULL " +
            "WHERE user_id = #{userId}")
    int update(@Param("userId") Long userId, @Param("idCardNo") String idCardNo,
               @Param("skillTags") String skillTags, @Param("serviceRegion") String serviceRegion,
               @Param("dutyArea") String dutyArea);

    //待审核志愿者列表（管理员审核页）
    @Select("SELECT v.user_id AS id, u.username, u.real_name, u.phone, v.id_card_no, v.skill_tags, " +
            "v.service_region, v.duty_area, v.created_at AS apply_time " +
            "FROM volunteer_profiles v JOIN users u ON u.id = v.user_id " +
            "WHERE v.cert_status = 0 ORDER BY v.id")
    IPage<Map<String, Object>> findPendingCertify(Page<Map<String, Object>> page);

    //审核志愿者（通过/驳回）
    @Update("UPDATE volunteer_profiles SET cert_status = #{certStatus}, certify_reason = #{reason} WHERE user_id = #{userId}")
    int updateCertify(@Param("userId") Long userId, @Param("certStatus") Integer certStatus, @Param("reason") String reason);

    //待审核志愿者数量（看板统计）
    @Select("SELECT COUNT(*) FROM volunteer_profiles WHERE cert_status = 0")
    long countPending();

    //累计服务时长并刷新最近活跃时间（签退结算）
    @Update("UPDATE volunteer_profiles SET total_service_hours = total_service_hours + #{hours}, " +
            "last_active_time = NOW() WHERE user_id = #{userId}")
    int addServiceHours(@Param("userId") Long userId, @Param("hours") Integer hours);

    //刷新最近活跃时间（签到时）
    @Update("UPDATE volunteer_profiles SET last_active_time = NOW() WHERE user_id = #{userId}")
    int updateLastActive(@Param("userId") Long userId);

    //更新荣誉数据（患者评价后：星星总数 + 平均评分）
    @Update("UPDATE volunteer_profiles SET stars_total = #{starsTotal}, rating_score = #{ratingScore} WHERE user_id = #{userId}")
    int updateRating(@Param("userId") Long userId, @Param("starsTotal") Integer starsTotal,
                     @Param("ratingScore") BigDecimal ratingScore);
}
