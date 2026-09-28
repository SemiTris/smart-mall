package com.huashan.smartmallbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * <p>
 * 本项目的 axios（project/request.js）baseURL 是绝对地址 http://localhost:8080/，
 * 不走 Vite 代理，所以跨域必须由后端处理。
 * <p>
 * 注意：allowCredentials(true) 时不能用 allowedOrigins("*")，
 * Spring 会抛 IllegalArgumentException，必须用 allowedOriginPatterns。
 *
 * @author hs
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 注册全局跨域规则
     *
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 用 pattern 而非 origin，才能与 allowCredentials(true) 共存
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}