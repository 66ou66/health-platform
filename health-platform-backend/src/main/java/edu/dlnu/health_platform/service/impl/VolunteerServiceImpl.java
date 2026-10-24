package edu.dlnu.health_platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.dal.ServiceRequestMapper;
import edu.dlnu.health_platform.dal.TaskAssignmentMapper;
import edu.dlnu.health_platform.dal.UserMapper;
import edu.dlnu.health_platform.dal.VolunteerProfileMapper;
import edu.dlnu.health_platform.entity.TaskAssignment;
import edu.dlnu.health_platform.entity.User;
import edu.dlnu.health_platform.entity.VolunteerProfile;
import edu.dlnu.health_platform.service.FeedbackService;
import edu.dlnu.health_platform.service.VolunteerService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VolunteerServiceImpl implements VolunteerService {

    @Autowired
    private VolunteerProfileMapper volunteerProfileMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ServiceRequestMapper serviceRequestMapper;
    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;
    @Autowired
    private FeedbackService feedbackService;

    @Override
    @Transactional
    public Result<Void> updateProfile(Long userId, Map<String, Object> data) {
        if (data == null) {
            return Result.fail("档案信息不能为空");
        }
        String idCardNo = data.get("idCardNo") == null ? null : data.get("idCardNo").toString();
        if (idCardNo == null || idCardNo.isBlank()) {
            return Result.fail("身份证号不能为空（实名认证必填）");
        }
        //技能标签：前端传数组，转成逗号分隔字符串存储
        Object skillsObj = data.get("skillTags");
        String skillTags;
        if (skillsObj instanceof List<?> list) {
            skillTags = list.stream().map(String::valueOf).collect(Collectors.joining(","));
        } else {
            skillTags = skillsObj == null ? null : skillsObj.toString();
        }
        String serviceRegion = data.get("serviceRegion") == null ? null : data.get("serviceRegion").toString();
        String dutyArea = data.get("dutyArea") == null ? null : data.get("dutyArea").toString();
        if (volunteerProfileMapper.findByUserId(userId) == null) {
            volunteerProfileMapper.insert(userId);
        }
        //更新后认证状态回到待审核，等待管理员重新审核
        volunteerProfileMapper.update(userId, idCardNo, skillTags, serviceRegion, dutyArea);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getProfile(Long userId) {
        Map<String, Object> map = new HashMap<>();
        VolunteerProfile profile = volunteerProfileMapper.findByUserId(userId);
        if (profile != null) {
            map.put("id", profile.getId());
            map.put("idCardNo", profile.getIdCardNo());
            //技能标签：存储为逗号分隔，返回时拆成数组
            map.put("skillTags", profile.getSkillTags() == null || profile.getSkillTags().isBlank()
                    ? List.of()
                    : Arrays.asList(profile.getSkillTags().split(",")));
            map.put("serviceRegion", profile.getServiceRegion());
            map.put("dutyArea", profile.getDutyArea());
            map.put("totalServiceHours", profile.getTotalServiceHours());
            map.put("starsTotal", profile.getStarsTotal());
            map.put("ratingScore", profile.getRatingScore());
            map.put("certStatus", profile.getCertStatus());
            map.put("certifyReason", profile.getCertifyReason());
            map.put("lastActiveTime", profile.getLastActiveTime());
        }
        User user = userMapper.findById(userId);
        if (user != null) {
            map.put("realName", user.getRealName());
            map.put("phone", user.getPhone());
        }
        return Result.success(map);
    }

    @Override
    public Result<PageResult<Map<String, Object>>> getPendingTasks(Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(serviceRequestMapper.findPending(new Page<>(pageNum, pageSize))));
    }

    @Override
    @Transactional
    public Result<Void> claim(Long volunteerId, Long requestId) {
        if (requestId == null) {
            return Result.fail("需求ID不能为空");
        }
        //仅当需求还是待接单状态时抢单成功（防止并发抢同一单）
        int rows = serviceRequestMapper.claim(requestId);
        if (rows == 0) {
            return Result.fail("手慢了，该需求已被其他志愿者接走");
        }
        TaskAssignment task = new TaskAssignment();
        task.setRequestId(requestId);
        task.setVolunteerId(volunteerId);
        task.setAssignType(1);
        task.setAcceptTime(LocalDateTime.now());
        taskAssignmentMapper.insert(task);
        return Result.success();
    }

    @Override
    public Result<PageResult<Map<String, Object>>> getPendingCertifyList(Integer pageNum, Integer pageSize) {
        IPage<Map<String, Object>> page = volunteerProfileMapper.findPendingCertify(new Page<>(pageNum, pageSize));
        //技能标签拆成数组返回，方便前端直接渲染（只处理当前页）
        for (Map<String, Object> row : page.getRecords()) {
            Object skills = row.get("skill_tags");
            row.put("skillTags", skills == null || skills.toString().isBlank()
                    ? List.of()
                    : Arrays.asList(skills.toString().split(",")));
        }
        return Result.success(PageResult.of(page));
    }

    @Override
    public Result<Void> certify(Long userId, Boolean approved, String reason) {
        VolunteerProfile profile = volunteerProfileMapper.findByUserId(userId);
        if (profile == null) {
            return Result.fail("志愿者档案不存在");
        }
        if (approved == null) {
            return Result.fail("审核结果不能为空");
        }
        if (!approved && (reason == null || reason.isBlank())) {
            return Result.fail("驳回时必须填写驳回原因");
        }
        volunteerProfileMapper.updateCertify(userId, approved ? 1 : 2, approved ? null : reason);
        return Result.success();
    }

    @Override
    public Result<Void> submitFeedback(Long volunteerId, FeedbackReqVo vo) {
        return feedbackService.submit(volunteerId, "志愿者", vo);
    }

    @Override
    public long countPendingCertify() {
        return volunteerProfileMapper.countPending();
    }
}
