package edu.dlnu.health_platform.config;

import edu.dlnu.health_platform.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 * 注：CORS 跨域已按课程示例风格改为各 Controller 上的 @CrossOrigin 注解处理；
 * 本类负责注册 JWT 登录拦截器（/auth/** 免登录）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/error");
    }
}
