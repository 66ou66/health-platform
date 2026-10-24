package edu.dlnu.health_platform.controller;

import edu.dlnu.health_platform.entity.Category;
import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.service.CategoryService;
import edu.dlnu.health_platform.service.HealthRecordService;
import edu.dlnu.health_platform.service.PatientService;
import edu.dlnu.health_platform.service.SosAlertService;
import edu.dlnu.health_platform.service.TaskAssignmentService;
import edu.dlnu.health_platform.service.UserService;
import edu.dlnu.health_platform.service.VolunteerService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员端接口（/admin）：用户管理、志愿者审核、患者档案、分类管理、派单/改派、SOS 处理、数据看板
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SosAlertService sosAlertService;
    @Autowired
    private TaskAssignmentService taskAssignmentService;
    @Autowired
    private HealthRecordService healthRecordService;

    //获取用户列表（分页，role 为空时查全部角色）
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(@RequestParam(required = false) Integer role,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return userService.getAllUsers(role, pageNum, pageSize);
    }

    //冻结/解冻用户（1正常 0冻结）
    @PutMapping("/user/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Object> data) {
        Object status = data == null ? null : data.get("status");
        return userService.updateStatus(userId, status == null ? null : Integer.parseInt(String.valueOf(status)));
    }

    //待审核志愿者列表（分页）
    @GetMapping("/volunteers/pending")
    public Result<PageResult<Map<String, Object>>> getPendingVolunteers(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return volunteerService.getPendingCertifyList(pageNum, pageSize);
    }

    //审核志愿者
    @PutMapping("/volunteer/{userId}/certify")
    public Result<Void> certifyVolunteer(@PathVariable Long userId, @RequestBody Map<String, Object> data) {
        Object approved = data == null ? null : data.get("approved");
        Object reason = data == null ? null : data.get("reason");
        return volunteerService.certify(userId,
                approved == null ? null : Boolean.parseBoolean(String.valueOf(approved)),
                reason == null ? null : String.valueOf(reason));
    }

    //查看患者完整档案（含身份证号、慢性病、服药清单等敏感字段）
    @GetMapping("/patient/{userId}/full-profile")
    public Result<Map<String, Object>> getPatientFullProfile(@PathVariable Long userId) {
        return patientService.getFullProfile(userId);
    }

    //服务分类列表
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        return categoryService.list();
    }

    //新增分类
    @PostMapping("/category")
    public Result<Void> addCategory(@RequestBody Category category) {
        return categoryService.add(category);
    }

    //修改分类
    @PutMapping("/category")
    public Result<Void> updateCategory(@RequestBody Category category) {
        return categoryService.update(category);
    }

    //删除分类
    @DeleteMapping("/category/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        return categoryService.delete(categoryId);
    }

    //待派单需求列表（派单管理页，与任务大厅同数据源，分页）
    @GetMapping("/requests/pending")
    public Result<PageResult<Map<String, Object>>> getPendingRequests(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return volunteerService.getPendingTasks(pageNum, pageSize);
    }

    //全部任务列表（派单管理页，分页）
    @GetMapping("/tasks")
    public Result<PageResult<Map<String, Object>>> getTasks(@RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return taskAssignmentService.listAll(pageNum, pageSize);
    }

    //系统派单：将待接单需求直接派给指定志愿者
    @PostMapping("/task/assign")
    public Result<Void> assignTask(@RequestBody Map<String, Object> data) {
        Object requestId = data == null ? null : data.get("requestId");
        Object volunteerId = data == null ? null : data.get("volunteerId");
        return taskAssignmentService.assign(
                requestId == null ? null : Long.parseLong(String.valueOf(requestId)),
                volunteerId == null ? null : Long.parseLong(String.valueOf(volunteerId)));
    }

    //系统改派：更换任务志愿者
    @PutMapping("/task/{taskId}/reassign")
    public Result<Void> reassignTask(@PathVariable Long taskId, @RequestBody Map<String, Object> data) {
        Object volunteerId = data == null ? null : data.get("volunteerId");
        Object remark = data == null ? null : data.get("remark");
        return taskAssignmentService.reassign(taskId,
                volunteerId == null ? null : Long.parseLong(String.valueOf(volunteerId)),
                remark == null ? null : String.valueOf(remark));
    }

    //待处理 SOS 列表（分页）
    @GetMapping("/sos/pending")
    public Result<PageResult<Map<String, Object>>> getPendingSos(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return sosAlertService.getPending(pageNum, pageSize);
    }

    //指派志愿者处理 SOS
    @PutMapping("/sos/{id}/accept")
    public Result<Void> acceptSos(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Object volunteerId = data == null ? null : data.get("volunteerId");
        return sosAlertService.accept(id,
                volunteerId == null ? null : Long.parseLong(String.valueOf(volunteerId)));
    }

    //完成 SOS
    @PutMapping("/sos/{id}/complete")
    public Result<Void> completeSos(@PathVariable Long id) {
        return sosAlertService.complete(id);
    }

    //数据统计看板
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userService.countAll());
        map.put("patientCount", userService.countByRole(1));
        map.put("volunteerCount", userService.countByRole(2));
        map.put("pendingVolunteerCount", volunteerService.countPendingCertify());
        map.put("doingTaskCount", taskAssignmentService.countDoing());
        map.put("doneTaskCount", taskAssignmentService.countDone());
        map.put("pendingSosCount", sosAlertService.countPending());
        map.put("healthRecordCount", healthRecordService.countAll());
        return Result.success(map);
    }
}
