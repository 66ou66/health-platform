package edu.dlnu.health_platform.vo.request;

import lombok.Data;

/**
 * 登录入参
 */
@Data
public class LoginReqVo {
    private String userName;
    private String password;
    /** 角色：0管理员 1患者 2志愿者（与数据库 users.role 一致） */
    private Integer role;
}
