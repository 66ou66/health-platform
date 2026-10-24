package edu.dlnu.health_platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.dal.CategoryMapper;
import edu.dlnu.health_platform.dal.ServiceRequestMapper;
import edu.dlnu.health_platform.dal.TaskAssignmentMapper;
import edu.dlnu.health_platform.entity.ServiceRequest;
import edu.dlnu.health_platform.entity.TaskAssignment;
import edu.dlnu.health_platform.service.ServiceRequestService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.PublishRequestReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {

    @Autowired
    private ServiceRequestMapper serviceRequestMapper;
    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result<Void> publish(Long patientId, PublishRequestReqVo vo) {
        if (vo == null) {
            return Result.fail("需求信息不能为空");
        }
        if (vo.getCategoryId() == null) {
            return Result.fail("请选择服务类型");
        }
        if (categoryMapper.findById(vo.getCategoryId()) == null) {
            return Result.fail("服务类型不存在");
        }
        if (vo.getTitle() == null || vo.getTitle().isBlank()) {
            return Result.fail("请填写需求标题");
        }
        if (vo.getAddress() == null || vo.getAddress().isBlank()) {
            return Result.fail("请填写服务地址");
        }
        if (vo.getAppointmentTime() == null) {
            return Result.fail("请选择约定服务时间");
        }
        ServiceRequest request = new ServiceRequest();
        request.setPatientId(patientId);
        request.setCategoryId(vo.getCategoryId());
        request.setTitle(vo.getTitle());
        request.setDescription(vo.getDescription());
        request.setAddress(vo.getAddress());
        request.setAppointmentTime(vo.getAppointmentTime());
        request.setEmergencyFlag(vo.getEmergencyFlag() == null ? 0 : vo.getEmergencyFlag());
        serviceRequestMapper.insert(request);
        return Result.success();
    }

    @Override
    public Result<PageResult<Map<String, Object>>> listByPatient(Long patientId, Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(serviceRequestMapper.findByPatient(new Page<>(pageNum, pageSize), patientId)));
    }

    @Override
    public Result<Map<String, Object>> detail(Long patientId, Long requestId) {
        Map<String, Object> detail = serviceRequestMapper.findDetailById(requestId);
        if (detail == null) {
            return Result.fail("需求不存在");
        }
        //只能查看自己的需求
        Object owner = detail.get("patient_id");
        if (owner == null || Long.parseLong(String.valueOf(owner)) != patientId) {
            return Result.fail("无权查看该需求");
        }
        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<Void> cancel(Long patientId, Long requestId) {
        ServiceRequest request = serviceRequestMapper.findById(requestId);
        if (request == null) {
            return Result.fail("需求不存在");
        }
        if (!request.getPatientId().equals(patientId)) {
            return Result.fail("无权取消该需求");
        }
        //仅待接单/已接单状态可取消
        if (request.getStatus() != 0 && request.getStatus() != 1) {
            return Result.fail("当前状态不可取消");
        }
        serviceRequestMapper.updateStatus(requestId, 4);
        //已接单的同步把任务置为已取消(3)
        TaskAssignment task = taskAssignmentMapper.findByRequestId(requestId);
        if (task != null && (task.getStatus() == 0 || task.getStatus() == 1)) {
            taskAssignmentMapper.updateStatus(task.getId(), 3);
        }
        return Result.success();
    }
}
