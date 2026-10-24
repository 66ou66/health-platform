package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.LocationReqVo;

import java.util.Map;

public interface LocationService {

    //上报当前位置（患者/志愿者心跳）
    Result<Void> report(Long userId, LocationReqVo vo);

    //志愿者查询服务中任务的患者实时位置
    Result<Map<String, Object>> getPatientLocation(Long volunteerId, Long taskId);
}
