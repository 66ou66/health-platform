package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.SignInReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import edu.dlnu.health_platform.vo.response.TaskDetailRespVo;

import java.util.Map;

public interface TaskAssignmentService {

    //我的任务列表（志愿者，分页）
    Result<PageResult<Map<String, Object>>> listByVolunteer(Long volunteerId, Integer pageNum, Integer pageSize);

    //任务详情（志愿者，含患者脱敏信息）
    Result<TaskDetailRespVo> detail(Long volunteerId, Long taskId);

    //任务签到
    Result<Void> signIn(Long volunteerId, Long taskId, SignInReqVo vo);

    //任务签退（同时结算服务时长）
    Result<Void> signOut(Long volunteerId, Long taskId, SignInReqVo vo);

    //全部任务列表（管理员派单管理页，分页）
    Result<PageResult<Map<String, Object>>> listAll(Integer pageNum, Integer pageSize);

    //管理员指派：将待接单需求直接派给指定志愿者
    Result<Void> assign(Long requestId, Long volunteerId);

    //管理员改派：更换任务志愿者并记录原因
    Result<Void> reassign(Long taskId, Long volunteerId, String remark);

    //进行中任务数量（看板统计）
    long countDoing();

    //已完成任务数量（看板统计）
    long countDone();
}
