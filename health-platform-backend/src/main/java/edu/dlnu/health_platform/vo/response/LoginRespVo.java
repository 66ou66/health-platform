package edu.dlnu.health_platform.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 登录出参：token + 用户信息（id/userName/realName/role/phone）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespVo {
    private String token;
    private Map<String, Object> userInfo;
}
