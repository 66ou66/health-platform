package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;

import java.util.Map;

public interface VolunteerService {

    //完善/更新志愿者档案（含实名认证信息、技能标签）
    Result<Void> updateProfile(Long userId, Map<String, Object> data);

    //获取我的志愿者档案
    Result<Map<String, Object>> getProfile(Long userId);

    //任务大厅（待接单需求列表，含患者脱敏信息，分页）
    Result<PageResult<Map<String, Object>>> getPendingTasks(Integer pageNum, Integer pageSize);

    //抢单
    Result<Void> claim(Long volunteerId, Long requestId);

    //待审核志愿者列表（管理员，分页）
    Result<PageResult<Map<String, Object>>> getPendingCertifyList(Integer pageNum, Integer pageSize);

    //审核志愿者（管理员）
    Result<Void> certify(Long userId, Boolean approved, String reason);

    //志愿者评价患者
    Result<Void> submitFeedback(Long volunteerId, FeedbackReqVo vo);

    //待审核志愿者数量（看板统计）
    long countPendingCertify();
}
