package com.x.data.collection.service.adapter.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 注册设备认证拦截器
 *
 * @author whj
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private DeviceAuthInterceptor deviceAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册设备认证拦截器，拦截所有设备数据接口
        registry.addInterceptor(deviceAuthInterceptor)
                .addPathPatterns("/api/device/data/**");
    }
}