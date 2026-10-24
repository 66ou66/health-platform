package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务需求表实体（service_requests）
 * status：0待接单 1已接单 2服务中 3已完成 4已取消；emergency_flag：0普通 1紧急
 */
@Data
public class ServiceRequest {
    private Long id;
    /** 患者用户ID */
    private Long patientId;
    /** 服务分类ID */
    private Long categoryId;
    /** 需求标题 */
    private String title;
    private String description;
    /** 服务地址 */
    private String address;
    private BigDecimal serviceLongitude;
    private BigDecimal serviceLatitude;
    /** 约定服务时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
    /** 状态：0待接单 1已接单 2服务中 3已完成 4已取消 */
    private Integer status;
    /** 是否紧急：0普通 1紧急 */
    private Integer emergencyFlag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
