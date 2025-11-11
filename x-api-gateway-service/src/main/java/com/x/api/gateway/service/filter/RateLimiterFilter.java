//package com.x.api.gateway.service.filter;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
//import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
//import org.springframework.stereotype.Component;
//
///**
// * 限流过滤器工厂 - 用于创建限流过滤器
// * 扩展AbstractGatewayFilterFactory实现自定义限流逻辑
// *
// * @author whj
// */
//@Component
//public class RateLimiterFilter extends AbstractGatewayFilterFactory<RateLimiterFilter.Config> {
//
//    private static final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);
//
//    private final RedisRateLimiter redisRateLimiter;
//
//    @Autowired
//    public RateLimiterFilter(RedisRateLimiter redisRateLimiter) {
//        super(Config.class);
//        this.redisRateLimiter = redisRateLimiter;
//    }
//
//    @Override
//    public GatewayFilter apply(Config config) {
//        return (exchange, chain) -> {
//            // 使用配置的KeyResolver获取限流键
//            return config.keyResolver.resolve(exchange).flatMap(key -> {
//                // 调用RedisRateLimiter进行限流检查
//                return redisRateLimiter.isAllowed(config.routeId, key).flatMap(response -> {
//                    // 检查是否允许请求通过
//                    if (response.isAllowed()) {
//                        // 允许请求通过
//                        return chain.filter(exchange);
//                    } else {
//                        // 请求被限流，记录日志
//                        logger.warn("Rate limiting applied for key: {}, route: {}", key, config.routeId);
//
//                        // 设置限流响应状态码
//                        exchange.getResponse().setStatusCode(config.statusCode);
//                        return exchange.getResponse().setComplete();
//                    }
//                });
//            });
//        };
//    }
//
//    /**
//     * 限流过滤器配置类
//     */
//    @Setter
//    @Getter
//    public static class Config {
//        private String routeId;
//        private KeyResolver keyResolver;
//        private org.springframework.http.HttpStatus statusCode;
//
//        public Config() {
//            // 默认使用HTTP 429状态码（Too Many Requests）
//            this.statusCode = org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
//        }
//
//    }
//}