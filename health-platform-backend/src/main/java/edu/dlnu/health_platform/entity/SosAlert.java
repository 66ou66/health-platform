package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SOS 紧急求助表实体（sos_alerts）
 * status：0待处理 1已指派 2已完成
 */
@Data
public class SosAlert {
    private Long id;
    /** 患者用户ID */
    private Long patientId;
    /** 处理志愿者用户ID */
    private Long volunteerId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    /** 地址（高德逆地理编码） */
    private String address;
    /** 联系号码 */
    private String contactPhone;
    /** 情况备注 */
    private String remark;
    /** 状态：0待处理 1已指派 2已完成 */
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime triggerTime;
    /** 处理完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime responseTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
