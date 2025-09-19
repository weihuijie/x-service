package com.x.api.gateway.service.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义响应头过滤器工厂
 * 用于在响应中添加自定义头信息
 */
@Component
public class AddResponseHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<AddResponseHeaderGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AddResponseHeaderGatewayFilterFactory.class);
    
    public static final String NAME_KEY = "name";
    public static final String VALUE_KEY = "value";

    public AddResponseHeaderGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(NAME_KEY, VALUE_KEY);
    }

    @Override
    public GatewayFilter apply(Config config) {
        logger.info("Creating AddResponseHeaderGatewayFilter with name: {}, value: {}", 
                config.getName(), config.getValue());
        
        return (exchange, chain) -> {
            // 在响应返回客户端之前添加响应头
            ServerHttpResponse response = exchange.getResponse();
            
            // 添加配置的响应头
            response.getHeaders().add(config.getName(), config.getValue());
            
            // 记录添加的响应头信息
            logger.debug("Added response header: {}={}", config.getName(), config.getValue());
            
            // 执行过滤器链并确保响应头被添加
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // 响应完成后可以添加额外的处理逻辑
                logger.trace("Request completed, response header {}={} added", 
                        config.getName(), config.getValue());
            }));
        };
    }

    /**
     * 配置类
     */
    public static class Config {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}