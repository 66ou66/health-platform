package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者档案表实体（patient_profiles）
 * 敏感字段：id_card_no / chronic_diseases / medication_list，仅管理员可见
 * 楼栋/楼层/房间：用于院内就近派单
 */
@Data
public class PatientProfile {
    private Long id;
    private Long userId;
    /** 身份证号（敏感） */
    private String idCardNo;
    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    /** 性别：0女 1男 */
    private Integer gender;
    /** 紧急联系人姓名 */
    private String emergencyContactName;
    /** 紧急联系人电话 */
    private String emergencyContactPhone;
    /** 家庭住址 */
    private String homeAddress;
    private BigDecimal homeLongitude;
    private BigDecimal homeLatitude;
    /** 过敏史 */
    private String allergyHistory;
    /** 慢性病（敏感） */
    private String chronicDiseases;
    /** 服药清单（敏感） */
    private String medicationList;
    /** 楼栋（院内派单用） */
    private String building;
    /** 楼层（院内派单用） */
    private String floor;
    /** 房间号 */
    private String room;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
