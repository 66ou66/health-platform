package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿者档案表实体（volunteer_profiles）
 * 精神荣誉体系：total_service_hours 服务时长、stars_total 星星数、rating_score 平均评分
 */
@Data
public class VolunteerProfile {
    private Long id;
    private Long userId;
    /** 身份证号（实名认证） */
    private String idCardNo;
    /** 技能标签，逗号分隔 */
    private String skillTags;
    /** 服务区域 */
    private String serviceRegion;
    /** 值班区域 */
    private String dutyArea;
    /** 累计服务时长（小时） */
    private Integer totalServiceHours;
    /** 获得星星总数 */
    private Integer starsTotal;
    /** 平均评分（星星折算） */
    private BigDecimal ratingScore;
    /** 认证状态：0待审核 1已通过 2已驳回 */
    private Integer certStatus;
    /** 认证驳回原因 */
    private String certifyReason;
    /** 最近活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
