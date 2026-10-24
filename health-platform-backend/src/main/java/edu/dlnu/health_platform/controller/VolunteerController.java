package edu.dlnu.health_platform.controller;

import edu.dlnu.health_platform.service.LocationService;
import edu.dlnu.health_platform.service.TaskAssignmentService;
import edu.dlnu.health_platform.service.VolunteerService;
import edu.dlnu.health_platform.utils.UserContext;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import edu.dlnu.health_platform.vo.request.SignInReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import edu.dlnu.health_platform.vo.response.TaskDetailRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 志愿者端接口（/volunteer）
 */
@RestController
@RequestMapping("/volunteer")
@CrossOrigin
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private TaskAssignmentService taskAssignmentService;
    @Autowired
    private LocationService locationService;

    //完善/更新志愿者档案（含实名认证信息、技能标签）
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> data) {
        return volunteerService.updateProfile(UserContext.getUserId(), data);
    }

    //获取我的志愿者档案
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        return volunteerService.getProfile(UserContext.getUserId());
    }

    //任务大厅（待接单需求列表，分页）
    @GetMapping("/tasks/pending")
    public Result<PageResult<Map<String, Object>>> getPendingTasks(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return volunteerService.getPendingTasks(pageNum, pageSize);
    }

    //抢单
    @PostMapping("/task/{requestId}/claim")
    public Result<Void> claimTask(@PathVariable Long requestId) {
        return volunteerService.claim(UserContext.getUserId(), requestId);
    }

    //我的任务列表（分页）
    @GetMapping("/tasks/my")
    public Result<PageResult<Map<String, Object>>> getMyTasks(@RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return taskAssignmentService.listByVolunteer(UserContext.getUserId(), pageNum, pageSize);
    }

    //任务签到
    @PostMapping("/task/{taskId}/sign-in")
    public Result<Void> signIn(@PathVariable Long taskId, @RequestBody(required = false) SignInReqVo vo) {
        return taskAssignmentService.signIn(UserContext.getUserId(), taskId, vo);
    }

    //任务签退
    @PostMapping("/task/{taskId}/sign-out")
    public Result<Void> signOut(@PathVariable Long taskId, @RequestBody(required = false) SignInReqVo vo) {
        return taskAssignmentService.signOut(UserContext.getUserId(), taskId, vo);
    }

    //任务详情（含患者脱敏信息）
    @GetMapping("/task/{taskId}")
    public Result<TaskDetailRespVo> getTaskDetail(@PathVariable Long taskId) {
        return taskAssignmentService.detail(UserContext.getUserId(), taskId);
    }

    //查询服务中任务的患者实时位置（打车式实时地图数据源）
    @GetMapping("/task/{taskId}/patient-location")
    public Result<Map<String, Object>> getPatientLocation(@PathVariable Long taskId) {
        return locationService.getPatientLocation(UserContext.getUserId(), taskId);
    }

    //评价患者
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@RequestBody FeedbackReqVo vo) {
        return volunteerService.submitFeedback(UserContext.getUserId(), vo);
    }
}
