package edu.dlnu.health_platform.dal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.entity.HealthRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * health_records 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface HealthRecordMapper {

    //新增健康记录，返回受影响行数，主键回填
    @Insert("INSERT INTO health_records (patient_id, volunteer_id, record_time, blood_pressure_sys, " +
            "blood_pressure_dia, heart_rate, blood_sugar, mood_status, note) " +
            "VALUES (#{patientId}, #{volunteerId}, #{recordTime}, #{bloodPressureSys}, " +
            "#{bloodPressureDia}, #{heartRate}, #{bloodSugar}, #{moodStatus}, #{note})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HealthRecord record);

    //患者的健康记录列表（按测量时间倒序）
    @Select("SELECT * FROM health_records WHERE patient_id = #{patientId} ORDER BY record_time DESC, id DESC")
    IPage<HealthRecord> findByPatient(Page<HealthRecord> page, @Param("patientId") Long patientId);

    //健康记录总数（看板统计）
    @Select("SELECT COUNT(*) FROM health_records")
    long countAll();
}
