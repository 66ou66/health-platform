package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康监测记录表实体（health_records）
 */
@Data
public class HealthRecord {
    private Long id;
    /** 患者用户ID */
    private Long patientId;
    /** 上报的志愿者用户ID（志愿者代报时为非空） */
    private Long volunteerId;
    /** 测量时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
    /** 收缩压（高压）mmHg */
    private Integer bloodPressureSys;
    /** 舒张压（低压）mmHg */
    private Integer bloodPressureDia;
    /** 心率 次/分 */
    private Integer heartRate;
    /** 血糖 mmol/L */
    private BigDecimal bloodSugar;
    /** 心情状态：开心/平静/焦虑/低落 */
    private String moodStatus;
    private String note;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
