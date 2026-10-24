package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务指派表实体（task_assignments）
 * status：0已接单(待签到) 1服务中(已签到) 2已完成(已签退) 3已取消
 * assign_type：1志愿者抢单 2管理员指派
 */
@Data
public class TaskAssignment {
    private Long id;
    /** 需求ID */
    private Long requestId;
    /** 志愿者用户ID */
    private Long volunteerId;
    /** 指派方式：1志愿者抢单 2管理员指派 */
    private Integer assignType;
    /** 接单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signInTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signOutTime;
    private BigDecimal signInLongitude;
    private BigDecimal signInLatitude;
    /** 状态：0已接单(待签到) 1服务中 2已完成 3已取消 */
    private Integer status;
    /** 管理员改派备注 */
    private String adminRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
