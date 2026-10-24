package edu.dlnu.health_platform.controller;

import edu.dlnu.health_platform.entity.Category;
import edu.dlnu.health_platform.entity.HealthRecord;
import edu.dlnu.health_platform.entity.PatientProfile;
import edu.dlnu.health_platform.service.HealthRecordService;
import edu.dlnu.health_platform.service.LocationService;
import edu.dlnu.health_platform.service.PatientService;
import edu.dlnu.health_platform.service.ServiceRequestService;
import edu.dlnu.health_platform.utils.UserContext;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import edu.dlnu.health_platform.vo.request.LocationReqVo;
import edu.dlnu.health_platform.vo.request.PublishRequestReqVo;
import edu.dlnu.health_platform.vo.request.SosReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 患者端接口（/patient）
 */
@RestController
@RequestMapping("/patient")
@CrossOrigin
public class PatientController {

    @Autowired
    private PatientService patientService;
    @Autowired
    private ServiceRequestService serviceRequestService;
    @Autowired
    private HealthRecordService healthRecordService;
    @Autowired
    private LocationService locationService;

    //完善/更新我的健康档案
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody PatientProfile profile) {
        return patientService.updateProfile(UserContext.getUserId(), profile);
    }

    //获取我的健康档案
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        return patientService.getProfile(UserContext.getUserId());
    }

    //发布服务需求
    @PostMapping("/request")
    public Result<Void> publishRequest(@RequestBody PublishRequestReqVo vo) {
        return serviceRequestService.publish(UserContext.getUserId(), vo);
    }

    //我的需求列表（分页）
    @GetMapping("/requests")
    public Result<PageResult<Map<String, Object>>> getMyRequests(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return serviceRequestService.listByPatient(UserContext.getUserId(), pageNum, pageSize);
    }

    //需求详情
    @GetMapping("/request/{requestId}")
    public Result<Map<String, Object>> getRequestDetail(@PathVariable Long requestId) {
        return serviceRequestService.detail(UserContext.getUserId(), requestId);
    }

    //取消需求
    @DeleteMapping("/request/{requestId}")
    public Result<Void> cancelRequest(@PathVariable Long requestId) {
        return serviceRequestService.cancel(UserContext.getUserId(), requestId);
    }

    //我的健康记录（分页）
    @GetMapping("/health-records")
    public Result<PageResult<HealthRecord>> getHealthRecords(@RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return healthRecordService.listByPatient(UserContext.getUserId(), pageNum, pageSize);
    }

    //添加健康记录
    @PostMapping("/health-record")
    public Result<Void> addHealthRecord(@RequestBody HealthRecord record) {
        return healthRecordService.add(UserContext.getUserId(), record);
    }

    //SOS 一键求助（携带经纬度，后端逆地理编码解析地址）
    @PostMapping("/sos")
    public Result<Void> sendSos(@RequestBody SosReqVo vo) {
        return patientService.sendSos(UserContext.getUserId(), vo);
    }

    //位置心跳上报（服务中每5秒上报一次，供志愿者查看实时位置）
    @PostMapping("/location")
    public Result<Void> reportLocation(@RequestBody LocationReqVo vo) {
        return locationService.report(UserContext.getUserId(), vo);
    }

    //公开的服务分类列表（发布需求时选择）
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        return patientService.getCategories();
    }

    //评价志愿者
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@RequestBody FeedbackReqVo vo) {
        return patientService.submitFeedback(UserContext.getUserId(), vo);
    }
}
