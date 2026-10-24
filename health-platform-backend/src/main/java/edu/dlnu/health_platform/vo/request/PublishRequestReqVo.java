package edu.dlnu.health_platform.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布服务需求入参
 */
@Data
public class PublishRequestReqVo {
    /** 服务分类ID */
    private Long categoryId;
    /** 需求标题 */
    private String title;
    /** 需求描述 */
    private String description;
    /** 服务地址 */
    private String address;
    /** 约定服务时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
    /** 是否紧急：0普通 1紧急 */
    private Integer emergencyFlag;
}
