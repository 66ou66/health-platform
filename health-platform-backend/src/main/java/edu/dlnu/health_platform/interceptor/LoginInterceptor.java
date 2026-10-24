package edu.dlnu.health_platform.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dlnu.health_platform.utils.JwtUtils;
import edu.dlnu.health_platform.utils.UserContext;
import edu.dlnu.health_platform.vo.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器
 * 1. 校验请求头中的 token（未携带/过期 → code 401，前端自动跳登录页）
 * 2. 按路径前缀校验角色权限（/admin/** 仅管理员0、/patient/** 仅患者1、/volunteer/** 仅志愿者2）
 * 3. 将当前用户写入 UserContext
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行（CORS 由 @CrossOrigin 注解处理）
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 1. 校验 token
        String token = request.getHeader("token");
        Claims claims;
        try {
            claims = jwtUtils.parseToken(token);
        } catch (Exception e) {
            return writeJson(response, new Result<>(401, "登录已过期，请重新登录", null));
        }

        Long userId = claims.get("userId", Long.class);
        Integer role = claims.get("role", Integer.class);
        if (userId == null || role == null) {
            return writeJson(response, new Result<>(401, "登录已过期，请重新登录", null));
        }

        // 2. 按路径前缀校验角色权限（0管理员 1患者 2志愿者）
        String path = request.getRequestURI();
        boolean roleOk = true;
        if (path.startsWith("/health_platform/admin/") && role != 0) {
            roleOk = false;
        } else if (path.startsWith("/health_platform/patient/") && role != 1) {
            roleOk = false;
        } else if (path.startsWith("/health_platform/volunteer/") && role != 2) {
            roleOk = false;
        }
        if (!roleOk) {
            return writeJson(response, Result.fail("无权限访问"));
        }

        // 3. 写入当前用户上下文
        UserContext.set(userId, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理线程变量
        UserContext.clear();
    }

    /** 直接向响应写入 JSON（HTTP 状态仍为 200，业务码由 code 字段区分，与统一返回结构一致） */
    private boolean writeJson(HttpServletResponse response, Result<?> result) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(MAPPER.writeValueAsString(result));
        return false;
    }
}
