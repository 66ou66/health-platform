package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.entity.Category;
import edu.dlnu.health_platform.entity.PatientProfile;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import edu.dlnu.health_platform.vo.request.SosReqVo;

import java.util.List;
import java.util.Map;

public interface PatientService {

    //完善/更新我的健康档案
    Result<Void> updateProfile(Long userId, PatientProfile profile);

    //获取我的健康档案
    Result<Map<String, Object>> getProfile(Long userId);

    //获取患者完整档案（管理员，含敏感字段）
    Result<Map<String, Object>> getFullProfile(Long userId);

    //患者触发 SOS 紧急求助
    Result<Void> sendSos(Long userId, SosReqVo vo);

    //获取公开的服务分类列表
    Result<List<Category>> getCategories();

    //患者评价志愿者
    Result<Void> submitFeedback(Long userId, FeedbackReqVo vo);
}
