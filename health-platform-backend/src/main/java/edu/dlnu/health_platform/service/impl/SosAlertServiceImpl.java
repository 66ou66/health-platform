package edu.dlnu.health_platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.dal.PatientProfileMapper;
import edu.dlnu.health_platform.dal.SosAlertMapper;
import edu.dlnu.health_platform.dal.UserMapper;
import edu.dlnu.health_platform.dal.VolunteerProfileMapper;
import edu.dlnu.health_platform.entity.PatientProfile;
import edu.dlnu.health_platform.entity.SosAlert;
import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.entity.VolunteerProfile;
import edu.dlnu.health_platform.service.SosAlertService;
import edu.dlnu.health_platform.utils.AmapUtils;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.SosReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SosAlertServiceImpl implements SosAlertService {

    @Autowired
    private SosAlertMapper sosAlertMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PatientProfileMapper patientProfileMapper;
    @Autowired
    private VolunteerProfileMapper volunteerProfileMapper;
    @Autowired
    private AmapUtils amapUtils;

    @Override
    public Result<Void> create(Long patientId, SosReqVo vo) {
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
        User user = userMapper.findById(patientId);
        String contactPhone = user == null ? null : user.getPhone();
        if (contactPhone == null || contactPhone.isBlank()) {
            PatientProfile profile = patientProfileMapper.findByUserId(patientId);
            contactPhone = profile == null ? null : profile.getEmergencyContactPhone();
        }
        if (contactPhone == null || contactPhone.isBlank()) {
            return Result.fail("请先在档案中完善联系电话");
        }
        SosAlert sos = new SosAlert();
        sos.setPatientId(patientId);
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
    public Result<PageResult<Map<String, Object>>> getPending(Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(sosAlertMapper.findPending(new Page<>(pageNum, pageSize))));
    }

    @Override
    public Result<Void> accept(Long sosId, Long volunteerId) {
        SosAlert sos = sosAlertMapper.findById(sosId);
        if (sos == null) {
            return Result.fail("求助记录不存在");
        }
        if (sos.getStatus() != 0 && sos.getStatus() != 1) {
            return Result.fail("该求助已完成");
        }
        //仅已通过认证的志愿者可被指派
        if (volunteerId == null) {
            return Result.fail("请选择志愿者");
        }
        User user = userMapper.findById(volunteerId);
        if (user == null || user.getRole() == null || user.getRole() != 2) {
            return Result.fail("请选择志愿者账号");
        }
        VolunteerProfile profile = volunteerProfileMapper.findByUserId(volunteerId);
        if (profile == null || profile.getCertStatus() == null || profile.getCertStatus() != 1) {
            return Result.fail("该志愿者尚未通过认证，不能指派");
        }
        sosAlertMapper.updateAccept(sosId, volunteerId);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> complete(Long sosId) {
        SosAlert sos = sosAlertMapper.findById(sosId);
        if (sos == null) {
            return Result.fail("求助记录不存在");
        }
        if (sos.getStatus() != 0 && sos.getStatus() != 1) {
            return Result.fail("该求助已完成");
        }
        sosAlertMapper.updateComplete(sosId, LocalDateTime.now());
        return Result.success();
    }

    @Override
    public long countPending() {
        return sosAlertMapper.countPending();
    }
}
