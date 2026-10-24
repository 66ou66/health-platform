package edu.dlnu.health_platform.dal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.entity.TaskAssignment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * task_assignments 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface TaskAssignmentMapper {

    //新增任务，返回受影响行数，主键回填
    @Insert("INSERT INTO task_assignments (request_id, volunteer_id, assign_type, accept_time, status) " +
            "VALUES (#{requestId}, #{volunteerId}, #{assignType}, #{acceptTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskAssignment task);

    //根据需求ID查询任务
    @Select("SELECT * FROM task_assignments WHERE request_id = #{requestId}")
    TaskAssignment findByRequestId(@Param("requestId") Long requestId);

    //根据ID查询任务
    @Select("SELECT * FROM task_assignments WHERE id = #{id}")
    TaskAssignment findById(@Param("id") Long id);

    //我的任务列表（志愿者视角，关联分类名与患者姓名）
    @Select("SELECT t.id, t.request_id, t.status, t.sign_in_time, t.sign_out_time, t.created_at, " +
            "c.category_name, u.real_name AS patient_name, r.title, r.appointment_time, r.address, r.emergency_flag " +
            "FROM task_assignments t " +
            "JOIN service_requests r ON r.id = t.request_id " +
            "JOIN service_categories c ON c.id = r.category_id " +
            "JOIN users u ON u.id = r.patient_id " +
            "WHERE t.volunteer_id = #{volunteerId} ORDER BY t.id DESC")
    IPage<Map<String, Object>> findByVolunteer(Page<Map<String, Object>> page, @Param("volunteerId") Long volunteerId);

    //任务详情（志愿者视角，患者脱敏信息：姓名/出生日期/性别/过敏史/紧急联系人）
    @Select("SELECT t.id, t.request_id, t.status, t.sign_in_time, t.sign_out_time, " +
            "r.title, r.description, r.address, r.appointment_time, c.category_name, " +
            "u.real_name AS patient_name, pp.birthday AS patient_birthday, pp.gender AS patient_gender, " +
            "pp.allergy_history, pp.emergency_contact_name, pp.emergency_contact_phone " +
            "FROM task_assignments t " +
            "JOIN service_requests r ON r.id = t.request_id " +
            "JOIN service_categories c ON c.id = r.category_id " +
            "JOIN users u ON u.id = r.patient_id " +
            "LEFT JOIN patient_profiles pp ON pp.user_id = r.patient_id " +
            "WHERE t.id = #{id}")
    Map<String, Object> findDetailById(@Param("id") Long id);

    //更新任务状态
    @Update("UPDATE task_assignments SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    //签到：仅已接单(0)状态可签，签后进入服务中(1)
    @Update("UPDATE task_assignments SET sign_in_time = #{time}, sign_in_longitude = #{longitude}, " +
            "sign_in_latitude = #{latitude}, status = 1 " +
            "WHERE id = #{taskId} AND volunteer_id = #{volunteerId} AND status = 0")
    int signIn(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId,
               @Param("time") LocalDateTime time, @Param("longitude") BigDecimal longitude,
               @Param("latitude") BigDecimal latitude);

    //签退：仅服务中(1)状态可签，签后任务完成(2)
    @Update("UPDATE task_assignments SET sign_out_time = #{time}, status = 2 " +
            "WHERE id = #{taskId} AND volunteer_id = #{volunteerId} AND status = 1")
    int signOut(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId,
                @Param("time") LocalDateTime time);

    //管理员改派：更换志愿者并记录备注
    @Update("UPDATE task_assignments SET volunteer_id = #{volunteerId}, admin_remark = #{remark} " +
            "WHERE id = #{taskId} AND status IN (0, 1)")
    int reassign(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId,
                 @Param("remark") String remark);

    //全部任务列表（管理员派单管理页）
    @Select("SELECT t.id, t.request_id, t.volunteer_id, t.assign_type, t.status, t.admin_remark, " +
            "t.accept_time, t.sign_in_time, t.sign_out_time, c.category_name, r.title, " +
            "u.real_name AS patient_name, v.real_name AS volunteer_name " +
            "FROM task_assignments t " +
            "JOIN service_requests r ON r.id = t.request_id " +
            "JOIN service_categories c ON c.id = r.category_id " +
            "JOIN users u ON u.id = r.patient_id " +
            "JOIN users v ON v.id = t.volunteer_id " +
            "ORDER BY t.id DESC")
    IPage<Map<String, Object>> findAllForAdmin(Page<Map<String, Object>> page);

    //进行中任务数量（已接单+服务中，看板统计）
    @Select("SELECT COUNT(*) FROM task_assignments WHERE status IN (0, 1)")
    long countDoing();

    //按状态统计任务数量
    @Select("SELECT COUNT(*) FROM task_assignments WHERE status = #{status}")
    long countByStatus(@Param("status") Integer status);
}
