package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.SosReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;

import java.util.Map;

public interface SosAlertService {

    //患者触发 SOS 求助（后端逆地理编码解析地址）
    Result<Void> create(Long patientId, SosReqVo vo);

    //待处理 SOS 列表（管理员，分页）
    Result<PageResult<Map<String, Object>>> getPending(Integer pageNum, Integer pageSize);

    //指派志愿者处理（管理员）
    Result<Void> accept(Long sosId, Long volunteerId);

    //完成 SOS（管理员）
    Result<Void> complete(Long sosId);

    //待处理 SOS 数量（看板统计）
    long countPending();
}
