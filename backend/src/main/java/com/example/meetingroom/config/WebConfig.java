package com.example.meetingroom.config;

import com.example.meetingroom.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 注册登录拦截器与跨域配置（前端 5173 端口访问后端 8080）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    /** 注册登录拦截器：/api/** 全部校验，只放行登录与注册 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register"
                );
    }

    /** 跨域配置：允许前端开发服务器带凭证访问后端接口 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
