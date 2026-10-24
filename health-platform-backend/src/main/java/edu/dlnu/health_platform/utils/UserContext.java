package edu.dlnu.health_platform.utils;

/**
 * 当前登录用户上下文
 * LoginInterceptor 校验通过后写入，请求结束后清理
 */
public class UserContext {

    /** 当前登录用户ID */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    /** 当前登录角色：0管理员 1患者 2志愿者 */
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    public static void set(Long userId, Integer role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Integer getRole() {
        return ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
