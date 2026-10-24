package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价反馈表实体（feedbacks）
 * 一条任务记录同时包含患者、志愿者双方的评价（assignment_id 唯一）
 */
@Data
public class Feedback {
    private Long id;
    /** 任务ID */
    private Long assignmentId;
    /** 患者给志愿者的评分 1-5 星 */
    private Integer patientScore;
    /** 患者评价内容 */
    private String patientComment;
    /** 志愿者给患者的评分 1-5 星 */
    private Integer volunteerScore;
    /** 志愿者评价内容 */
    private String volunteerComment;
    /** 是否公开：1公开 0不公开 */
    private Integer isPublic;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
