package edu.dlnu.health_platform.service.impl;

import edu.dlnu.health_platform.dal.CategoryMapper;
import edu.dlnu.health_platform.dal.PatientProfileMapper;
import edu.dlnu.health_platform.dal.SosAlertMapper;
import edu.dlnu.health_platform.dal.UserMapper;
import edu.dlnu.health_platform.entity.Category;
import edu.dlnu.health_platform.entity.PatientProfile;
import edu.dlnu.health_platform.entity.SosAlert;
import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.service.FeedbackService;
import edu.dlnu.health_platform.service.PatientService;
import edu.dlnu.health_platform.utils.AmapUtils;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import edu.dlnu.health_platform.vo.request.SosReqVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientProfileMapper patientProfileMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SosAlertMapper sosAlertMapper;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private AmapUtils amapUtils;

    @Override
    @Transactional
    public Result<Void> updateProfile(Long userId, PatientProfile profile) {
        if (profile == null) {
            return Result.fail("档案信息不能为空");
        }
        profile.setUserId(userId);
        if (patientProfileMapper.findByUserId(userId) == null) {
            //注册时异常导致档案缺失，补建一条
            patientProfileMapper.insert(userId);
        }
        patientProfileMapper.update(profile);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getProfile(Long userId) {
        Map<String, Object> map = new HashMap<>();
        PatientProfile profile = patientProfileMapper.findByUserId(userId);
        if (profile != null) {
            map.put("id", profile.getId());
            map.put("idCardNo", profile.getIdCardNo());
            map.put("birthday", profile.getBirthday());
            map.put("gender", profile.getGender());
            map.put("emergencyContactName", profile.getEmergencyContactName());
            map.put("emergencyContactPhone", profile.getEmergencyContactPhone());
            map.put("homeAddress", profile.getHomeAddress());
            map.put("allergyHistory", profile.getAllergyHistory());
            map.put("chronicDiseases", profile.getChronicDiseases());
            map.put("medicationList", profile.getMedicationList());
            map.put("building", profile.getBuilding());
            map.put("floor", profile.getFloor());
            map.put("room", profile.getRoom());
        }
        User user = userMapper.findById(userId);
        if (user != null) {
            map.put("realName", user.getRealName());
            map.put("phone", user.getPhone());
        }
        return Result.success(map);
    }

    @Override
    public Result<Map<String, Object>> getFullProfile(Long userId) {
        //管理员视角：患者全部信息（含敏感字段）
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        PatientProfile profile = patientProfileMapper.findByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("realName", user.getRealName());
        map.put("phone", user.getPhone());
        if (profile != null) {
            map.put("idCardNo", profile.getIdCardNo());
            map.put("birthday", profile.getBirthday());
            map.put("gender", profile.getGender());
            map.put("emergencyContactName", profile.getEmergencyContactName());
            map.put("emergencyContactPhone", profile.getEmergencyContactPhone());
            map.put("homeAddress", profile.getHomeAddress());
            map.put("allergyHistory", profile.getAllergyHistory());
            map.put("chronicDiseases", profile.getChronicDiseases());
            map.put("medicationList", profile.getMedicationList());
            map.put("building", profile.getBuilding());
            map.put("floor", profile.getFloor());
            map.put("room", profile.getRoom());
        }
        return Result.success(map);
    }

    @Override
    @Transactional
    public Result<Void> sendSos(Long userId, SosReqVo vo) {
        if (vo == null) {
            return Result.fail("求助信息不能为空");
        }
        if (vo.getLongitude() == null || vo.getLatitude() == null) {
            return Result.fail("定位失败，请允许浏览器获取位置后重试");
        }
        //地址为空时用高德逆地理编码解析
        String address = (vo.getAddress() == null || vo.getAddress().isBlank())
                ? amapUtils.reverseGeocode(vo.getLongitude().doubleValue(), vo.getLatitude().doubleValue())
                : vo.getAddress();
        //联系号码：优先本人手机号，其次紧急联系人电话
        User user = userMapper.findById(userId);
        String contactPhone = user != null ? user.getPhone() : null;
        if (contactPhone == null || contactPhone.isBlank()) {
            PatientProfile profile = patientProfileMapper.findByUserId(userId);
            contactPhone = profile == null ? null : profile.getEmergencyContactPhone();
        }
        if (contactPhone == null || contactPhone.isBlank()) {
            return Result.fail("请先在档案中完善联系电话");
        }
        SosAlert sos = new SosAlert();
        sos.setPatientId(userId);
        sos.setLongitude(vo.getLongitude());
        sos.setLatitude(vo.getLatitude());
        sos.setAddress(address);
        sos.setContactPhone(contactPhone);
        sos.setRemark(vo.getRemark());
        sos.setTriggerTime(LocalDateTime.now());
        sosAlertMapper.insert(sos);
        return Result.success();
    }

    @Override
    public Result<List<Category>> getCategories() {
        return Result.success(categoryMapper.findAll());
    }

    @Override
    public Result<Void> submitFeedback(Long userId, FeedbackReqVo vo) {
        return feedbackService.submit(userId, "患者", vo);
    }
}
