package edu.dlnu.health_platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.dal.ServiceRequestMapper;
import edu.dlnu.health_platform.dal.TaskAssignmentMapper;
import edu.dlnu.health_platform.dal.UserMapper;
import edu.dlnu.health_platform.dal.VolunteerProfileMapper;
import edu.dlnu.health_platform.entity.ServiceRequest;
import edu.dlnu.health_platform.entity.TaskAssignment;
import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.entity.VolunteerProfile;
import edu.dlnu.health_platform.service.TaskAssignmentService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.SignInReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import edu.dlnu.health_platform.vo.response.TaskDetailRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;
    @Autowired
    private ServiceRequestMapper serviceRequestMapper;
    @Autowired
    private VolunteerProfileMapper volunteerProfileMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<PageResult<Map<String, Object>>> listByVolunteer(Long volunteerId, Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(taskAssignmentMapper.findByVolunteer(new Page<>(pageNum, pageSize), volunteerId)));
    }

    @Override
    public Result<TaskDetailRespVo> detail(Long volunteerId, Long taskId) {
        TaskAssignment task = taskAssignmentMapper.findById(taskId);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        if (!task.getVolunteerId().equals(volunteerId)) {
            return Result.fail("无权查看该任务");
        }
        Map<String, Object> row = taskAssignmentMapper.findDetailById(taskId);
        TaskDetailRespVo vo = new TaskDetailRespVo();
        vo.setId(toLong(row.get("id")));
        vo.setRequestId(toLong(row.get("request_id")));
        vo.setCategoryName((String) row.get("category_name"));
        vo.setTitle((String) row.get("title"));
        vo.setAppointmentTime(toLocalDateTime(row.get("appointment_time")));
        vo.setAddress((String) row.get("address"));
        vo.setDescription((String) row.get("description"));
        vo.setStatus(toInt(row.get("status")));
        //患者脱敏信息（不含身份证号/慢性病/服药清单）
        vo.setPatientName((String) row.get("patient_name"));
        vo.setBirthday(toLocalDate(row.get("patient_birthday")));
        vo.setPatientGender(toInt(row.get("patient_gender")));
        vo.setAllergyHistory((String) row.get("allergy_history"));
        vo.setEmergencyContactName((String) row.get("emergency_contact_name"));
        vo.setEmergencyContactPhone((String) row.get("emergency_contact_phone"));
        vo.setSignInTime(toLocalDateTime(row.get("sign_in_time")));
        vo.setSignOutTime(toLocalDateTime(row.get("sign_out_time")));
        return Result.success(vo);
    }

    @Override
    @Transactional
    public Result<Void> signIn(Long volunteerId, Long taskId, SignInReqVo vo) {
        TaskAssignment task = taskAssignmentMapper.findById(taskId);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        if (!task.getVolunteerId().equals(volunteerId)) {
            return Result.fail("无权操作该任务");
        }
        int rows = taskAssignmentMapper.signIn(taskId, volunteerId, LocalDateTime.now(),
                vo == null || vo.getLongitude() == null ? null : BigDecimal.valueOf(vo.getLongitude()),
                vo == null || vo.getLatitude() == null ? null : BigDecimal.valueOf(vo.getLatitude()));
        if (rows == 0) {
            return Result.fail("当前状态不可签到");
        }
        //需求同步为服务中，志愿者刷新活跃时间
        serviceRequestMapper.updateStatus(task.getRequestId(), 2);
        volunteerProfileMapper.updateLastActive(volunteerId);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> signOut(Long volunteerId, Long taskId, SignInReqVo vo) {
        TaskAssignment task = taskAssignmentMapper.findById(taskId);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        if (!task.getVolunteerId().equals(volunteerId)) {
            return Result.fail("无权操作该任务");
        }
        int rows = taskAssignmentMapper.signOut(taskId, volunteerId, LocalDateTime.now());
        if (rows == 0) {
            return Result.fail("当前状态不可签退");
        }
        //需求同步为已完成
        serviceRequestMapper.updateStatus(task.getRequestId(), 3);
        //结算服务时长，累计到志愿者荣誉数据（不足1小时按1小时计）
        int hours = 1;
        if (task.getSignInTime() != null) {
            long minutes = Duration.between(task.getSignInTime(), LocalDateTime.now()).toMinutes();
            hours = (int) Math.max(1, Math.round(minutes / 60.0));
        }
        volunteerProfileMapper.addServiceHours(volunteerId, hours);
        return Result.success();
    }

    // ================= 管理员：派单与改派 =================

    @Override
    public Result<PageResult<Map<String, Object>>> listAll(Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(taskAssignmentMapper.findAllForAdmin(new Page<>(pageNum, pageSize))));
    }

    @Override
    @Transactional
    public Result<Void> assign(Long requestId, Long volunteerId) {
        ServiceRequest request = serviceRequestMapper.findById(requestId);
        if (request == null) {
            return Result.fail("需求不存在");
        }
        if (request.getStatus() != 0) {
            return Result.fail("该需求已被接单或已完成");
        }
        if (!isActiveVolunteer(volunteerId)) {
            return Result.fail("请选择已通过认证的志愿者");
        }
        //标记需求为已接单（与抢单共用，防止并发）
        if (serviceRequestMapper.claim(requestId) == 0) {
            return Result.fail("该需求刚刚已被接走");
        }
        TaskAssignment task = new TaskAssignment();
        task.setRequestId(requestId);
        task.setVolunteerId(volunteerId);
        task.setAssignType(2);
        task.setAcceptTime(LocalDateTime.now());
        taskAssignmentMapper.insert(task);
        return Result.success();
    }

    @Override
    public Result<Void> reassign(Long taskId, Long volunteerId, String remark) {
        TaskAssignment task = taskAssignmentMapper.findById(taskId);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        if (task.getStatus() != 0 && task.getStatus() != 1) {
            return Result.fail("该任务已结束，无法改派");
        }
        if (remark == null || remark.isBlank()) {
            return Result.fail("请填写改派原因");
        }
        if (!isActiveVolunteer(volunteerId)) {
            return Result.fail("请选择已通过认证的志愿者");
        }
        if (taskAssignmentMapper.reassign(taskId, volunteerId, remark) == 0) {
            return Result.fail("改派失败，任务状态已变化");
        }
        return Result.success();
    }

    /** 校验志愿者是否可用（角色为志愿者且已通过认证） */
    private boolean isActiveVolunteer(Long volunteerId) {
        if (volunteerId == null) {
            return false;
        }
        User user = userMapper.findById(volunteerId);
        if (user == null || user.getRole() == null || user.getRole() != 2) {
            return false;
        }
        VolunteerProfile profile = volunteerProfileMapper.findByUserId(volunteerId);
        return profile != null && profile.getCertStatus() != null && profile.getCertStatus() == 1;
    }

    @Override
    public long countDoing() {
        return taskAssignmentMapper.countDoing();
    }

    @Override
    public long countDone() {
        return taskAssignmentMapper.countByStatus(2);
    }

    // ---------- 类型转换小工具 ----------
    private Long toLong(Object value) {
        return value == null ? null : Long.parseLong(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        return value == null ? null : Integer.parseInt(String.valueOf(value));
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime time) {
            return time;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value).substring(0, 10));
    }
}
