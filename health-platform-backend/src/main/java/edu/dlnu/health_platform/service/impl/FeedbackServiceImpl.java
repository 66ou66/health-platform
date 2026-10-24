package edu.dlnu.health_platform.service.impl;

import edu.dlnu.health_platform.dal.FeedbackMapper;
import edu.dlnu.health_platform.dal.ServiceRequestMapper;
import edu.dlnu.health_platform.dal.TaskAssignmentMapper;
import edu.dlnu.health_platform.dal.VolunteerProfileMapper;
import edu.dlnu.health_platform.entity.Feedback;
import edu.dlnu.health_platform.entity.ServiceRequest;
import edu.dlnu.health_platform.entity.TaskAssignment;
import edu.dlnu.health_platform.entity.VolunteerProfile;
import edu.dlnu.health_platform.service.FeedbackService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.request.FeedbackReqVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 双向评价：一条任务记录同时保存患者、志愿者双方的评价（feedbacks.assignment_id 唯一）
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;
    @Autowired
    private ServiceRequestMapper serviceRequestMapper;
    @Autowired
    private VolunteerProfileMapper volunteerProfileMapper;

    @Override
    @Transactional
    public Result<Void> submit(Long fromUserId, String fromRole, FeedbackReqVo vo) {
        if (vo == null || vo.getTaskId() == null) {
            return Result.fail("评价信息不能为空");
        }
        if (vo.getRating() == null || vo.getRating() < 1 || vo.getRating() > 5) {
            return Result.fail("评分需在 1-5 星之间");
        }
        TaskAssignment task = taskAssignmentMapper.findById(vo.getTaskId());
        if (task == null) {
            return Result.fail("任务不存在");
        }
        if (task.getStatus() != 2) {
            return Result.fail("服务完成后才能评价");
        }
        ServiceRequest request = serviceRequestMapper.findById(task.getRequestId());
        int isPublic = vo.getIsPublic() == null ? 1 : vo.getIsPublic();

        Feedback exist = feedbackMapper.findByAssignmentId(vo.getTaskId());
        if ("患者".equals(fromRole)) {
            //患者评价接单的志愿者
            if (!request.getPatientId().equals(fromUserId)) {
                return Result.fail("无权评价该任务");
            }
            if (exist == null) {
                feedbackMapper.insertPatient(vo.getTaskId(), vo.getRating(), vo.getContent(), isPublic);
            } else if (exist.getPatientScore() != null) {
                return Result.fail("您已评价过该任务");
            } else {
                feedbackMapper.updatePatient(vo.getTaskId(), vo.getRating(), vo.getContent(), isPublic);
            }
            //更新志愿者荣誉数据：星星总数累加，平均评分重新计算
            updateVolunteerRating(task.getVolunteerId(), vo.getRating());
        } else if ("志愿者".equals(fromRole)) {
            //志愿者评价患者
            if (!task.getVolunteerId().equals(fromUserId)) {
                return Result.fail("无权评价该任务");
            }
            if (exist == null) {
                feedbackMapper.insertVolunteer(vo.getTaskId(), vo.getRating(), vo.getContent(), isPublic);
            } else if (exist.getVolunteerScore() != null) {
                return Result.fail("您已评价过该任务");
            } else {
                feedbackMapper.updateVolunteer(vo.getTaskId(), vo.getRating(), vo.getContent(), isPublic);
            }
        } else {
            return Result.fail("非法角色");
        }
        return Result.success();
    }

    /** 患者评分后更新志愿者星星数与平均分 */
    private void updateVolunteerRating(Long volunteerId, int rating) {
        VolunteerProfile profile = volunteerProfileMapper.findByUserId(volunteerId);
        if (profile == null) {
            return;
        }
        int starsTotal = (profile.getStarsTotal() == null ? 0 : profile.getStarsTotal()) + rating;
        long count = feedbackMapper.countByVolunteer(volunteerId);
        BigDecimal avg = BigDecimal.valueOf(starsTotal)
                .divide(BigDecimal.valueOf(Math.max(count, 1)), 2, RoundingMode.HALF_UP);
        volunteerProfileMapper.updateRating(volunteerId, starsTotal, avg);
    }
}
