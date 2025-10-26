package com.x.api.gateway.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * 跨域配置类 - 统一处理所有服务的跨域请求
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // 允许的请求头
        config.setAllowedHeaders(Collections.singletonList("*"));

        // 允许的请求方法
        config.setAllowedMethods(Collections.singletonList("*"));

        // 允许携带认证信息
        config.setAllowCredentials(true);

        // 预检请求的有效期
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}