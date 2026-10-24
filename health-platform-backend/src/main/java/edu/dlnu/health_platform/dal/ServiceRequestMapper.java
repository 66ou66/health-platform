package edu.dlnu.health_platform.dal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.entity.ServiceRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * service_requests 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface ServiceRequestMapper {

    //新增需求，返回受影响行数，主键回填
    @Insert("INSERT INTO service_requests (patient_id, category_id, title, description, address, appointment_time, emergency_flag, status) " +
            "VALUES (#{patientId}, #{categoryId}, #{title}, #{description}, #{address}, #{appointmentTime}, #{emergencyFlag}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceRequest request);

    //患者的需求列表（关联分类名、接单志愿者名、任务ID）
    @Select("SELECT r.*, c.category_name, v.real_name AS volunteer_name, t.id AS task_id " +
            "FROM service_requests r " +
            "LEFT JOIN service_categories c ON c.id = r.category_id " +
            "LEFT JOIN task_assignments t ON t.request_id = r.id " +
            "LEFT JOIN users v ON v.id = t.volunteer_id " +
            "WHERE r.patient_id = #{patientId} ORDER BY r.id DESC")
    IPage<Map<String, Object>> findByPatient(Page<Map<String, Object>> page, @Param("patientId") Long patientId);

    //需求详情（同上，单条）
    @Select("SELECT r.*, c.category_name, v.real_name AS volunteer_name, t.id AS task_id " +
            "FROM service_requests r " +
            "LEFT JOIN service_categories c ON c.id = r.category_id " +
            "LEFT JOIN task_assignments t ON t.request_id = r.id " +
            "LEFT JOIN users v ON v.id = t.volunteer_id " +
            "WHERE r.id = #{id}")
    Map<String, Object> findDetailById(@Param("id") Long id);

    //根据ID查询需求
    @Select("SELECT * FROM service_requests WHERE id = #{id}")
    ServiceRequest findById(@Param("id") Long id);

    //更新需求状态
    @Update("UPDATE service_requests SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    //抢单（仅当状态为待接单时更新成功，防止并发抢同一单）
    @Update("UPDATE service_requests SET status = 1 WHERE id = #{id} AND status = 0")
    int claim(@Param("id") Long id);

    //任务大厅：待接单需求列表（紧急需求优先，含患者脱敏信息：姓名/出生日期/性别/过敏史/紧急联系人）
    @Select("SELECT r.id, r.category_id, c.category_name, r.title, r.description, r.address, r.appointment_time, " +
            "r.emergency_flag, r.created_at, u.real_name AS patient_name, pp.birthday AS patient_birthday, " +
            "pp.gender AS patient_gender, pp.allergy_history, pp.emergency_contact_name, pp.emergency_contact_phone " +
            "FROM service_requests r " +
            "JOIN service_categories c ON c.id = r.category_id " +
            "JOIN users u ON u.id = r.patient_id " +
            "LEFT JOIN patient_profiles pp ON pp.user_id = r.patient_id " +
            "WHERE r.status = 0 ORDER BY r.emergency_flag DESC, r.id DESC")
    IPage<Map<String, Object>> findPending(Page<Map<String, Object>> page);

    //按状态统计需求数量
    @Select("SELECT COUNT(*) FROM service_requests WHERE status = #{status}")
    long countByStatus(@Param("status") Integer status);
}
