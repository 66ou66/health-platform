package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.PublishRequestReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;

import java.util.Map;

public interface ServiceRequestService {

    //发布服务需求（患者）
    Result<Void> publish(Long patientId, PublishRequestReqVo vo);

    //我的需求列表（患者，分页）
    Result<PageResult<Map<String, Object>>> listByPatient(Long patientId, Integer pageNum, Integer pageSize);

    //需求详情（患者）
    Result<Map<String, Object>> detail(Long patientId, Long requestId);

    //取消需求（患者）
    Result<Void> cancel(Long patientId, Long requestId);
}
