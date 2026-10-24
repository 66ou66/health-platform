package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基础表实体（users）
 * role：0管理员 1患者 2志愿者；status：1正常 0冻结
 */
@Data
public class User {
    private Long id;
    private String username;
    /** 密码不回传给前端 */
    @JsonIgnore
    private String password;
    private String realName;
    private String phone;
    /** 角色：0管理员 1患者 2志愿者 */
    private Integer role;
    /** 头像地址 */
    private String avatar;
    /** 状态：1正常 0冻结 */
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
