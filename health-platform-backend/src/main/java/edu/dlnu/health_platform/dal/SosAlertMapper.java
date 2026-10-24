package edu.dlnu.health_platform.dal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.entity.SosAlert;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * sos_alerts 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface SosAlertMapper {

    //新增 SOS 求助，返回受影响行数，主键回填
    @Insert("INSERT INTO sos_alerts (patient_id, longitude, latitude, address, contact_phone, remark, trigger_time, status) " +
            "VALUES (#{patientId}, #{longitude}, #{latitude}, #{address}, #{contactPhone}, #{remark}, #{triggerTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SosAlert sos);

    //待处理 SOS 列表（含已指派未完成的，关联患者名与志愿者名）
    @Select("SELECT s.*, u.real_name AS patient_name, v.real_name AS volunteer_name " +
            "FROM sos_alerts s " +
            "JOIN users u ON u.id = s.patient_id " +
            "LEFT JOIN users v ON v.id = s.volunteer_id " +
            "WHERE s.status IN (0, 1) ORDER BY s.status, s.trigger_time DESC")
    IPage<Map<String, Object>> findPending(Page<Map<String, Object>> page);

    //根据ID查询
    @Select("SELECT * FROM sos_alerts WHERE id = #{id}")
    SosAlert findById(@Param("id") Long id);

    //指派志愿者处理
    @Update("UPDATE sos_alerts SET status = 1, volunteer_id = #{volunteerId} WHERE id = #{id} AND status IN (0, 1)")
    int updateAccept(@Param("id") Long id, @Param("volunteerId") Long volunteerId);

    //完成 SOS
    @Update("UPDATE sos_alerts SET status = 2, response_time = #{time} WHERE id = #{id} AND status IN (0, 1)")
    int updateComplete(@Param("id") Long id, @Param("time") LocalDateTime time);

    //待处理 SOS 数量（看板统计）
    @Select("SELECT COUNT(*) FROM sos_alerts WHERE status IN (0, 1)")
    long countPending();
}
