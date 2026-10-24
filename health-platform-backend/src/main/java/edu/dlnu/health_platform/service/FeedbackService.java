package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;

public interface FeedbackService {

    /**
     * 提交评价（双向）
     * @param fromUserId 评价人用户ID
     * @param fromRole   评价人角色：患者/志愿者（决定被评价人）
     * @param vo         评价内容
     */
    Result<Void> submit(Long fromUserId, String fromRole, FeedbackReqVo vo);
}
