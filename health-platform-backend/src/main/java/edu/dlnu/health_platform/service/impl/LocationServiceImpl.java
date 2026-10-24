package edu.dlnu.health_platform.service.impl;

import edu.dlnu.health_platform.dal.RealTimeLocationMapper;
import edu.dlnu.health_platform.dal.ServiceRequestMapper;
import edu.dlnu.health_platform.dal.TaskAssignmentMapper;
import edu.dlnu.health_platform.entity.RealTimeLocation;
import edu.dlnu.health_platform.entity.ServiceRequest;
import edu.dlnu.health_platform.entity.TaskAssignment;
import edu.dlnu.health_platform.service.LocationService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.LocationReqVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private RealTimeLocationMapper locationMapper;
    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;
    @Autowired
    private ServiceRequestMapper serviceRequestMapper;

    @Override
    public Result<Void> report(Long userId, LocationReqVo vo) {
        if (vo == null || vo.getLongitude() == null || vo.getLatitude() == null) {
            return Result.fail("定位信息不能为空");
        }
        locationMapper.report(userId, vo.getLongitude(), vo.getLatitude());
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getPatientLocation(Long volunteerId, Long taskId) {
        //1.任务必须存在且属于当前志愿者
        TaskAssignment task = taskAssignmentMapper.findById(taskId);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        if (!task.getVolunteerId().equals(volunteerId)) {
            return Result.fail("无权查看该任务的位置");
        }
        //2.仅"服务中"状态可查实时位置（防止任务结束后继续追踪患者）
        if (task.getStatus() != 1) {
            return Result.fail("仅服务中的任务可查看实时位置");
        }
        //3.查患者最新心跳
        ServiceRequest request = serviceRequestMapper.findById(task.getRequestId());
        RealTimeLocation location = locationMapper.findByUserId(request.getPatientId());
        if (location == null) {
            return Result.fail("患者尚未上报位置");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("longitude", location.getLongitude());
        map.put("latitude", location.getLatitude());
        map.put("updatedAt", location.getUpdatedAt());
        return Result.success(map);
    }
}
