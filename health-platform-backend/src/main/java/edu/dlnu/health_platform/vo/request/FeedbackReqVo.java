package edu.dlnu.health_platform.vo.request;

import lombok.Data;

/**
 * 服务评价入参（患者评价志愿者 / 志愿者评价患者）
 */
@Data
public class FeedbackReqVo {
    /** 任务ID（assignment_id） */
    private Long taskId;
    /** 评分 1-5 星 */
    private Integer rating;
    /** 评价内容 */
    private String content;
    /** 是否公开：1公开 0不公开 */
    private Integer isPublic;
}
