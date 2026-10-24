package edu.dlnu.health_platform.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务详情出参（志愿者视角）
 * 患者信息为脱敏字段：姓名/出生日期/性别/过敏史/紧急联系人，不含身份证号、慢性病、服药清单
 */
@Data
public class TaskDetailRespVo {
    private Long id;
    /** 需求ID */
    private Long requestId;
    /** 服务分类名称 */
    private String categoryName;
    /** 需求标题 */
    private String title;
    /** 约定服务时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
    private String address;
    private String description;
    /** 任务状态：0已接单(待签到) 1服务中 2已完成 3已取消 */
    private Integer status;
    // ---------- 患者脱敏信息 ----------
    private String patientName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private Integer patientGender;
    private String allergyHistory;
    private String emergencyContactName;
    private String emergencyContactPhone;
    // ---------- 签到信息 ----------
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signInTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signOutTime;
    private String signInAddress;
    private String signOutAddress;
}
