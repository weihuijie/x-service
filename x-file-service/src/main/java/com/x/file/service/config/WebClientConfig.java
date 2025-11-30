package com.x.file.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient配置类
 * 
 * @author whj
 */
@Configuration
public class WebClientConfig {

    /**
     * WebClient Builder Bean
     *
     * @return WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}