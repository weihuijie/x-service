//package com.x.api.gateway.service.filter;
//
//import org.slf4j.MDC;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.UUID;
//
///**
// * 请求追踪过滤器 - 为每个请求生成唯一的追踪ID，便于日志追踪和问题排查
// *
// * @author whj
// */
//@Component
//public class TraceFilter implements GlobalFilter, Ordered {
//
//    public static final String TRACE_ID = "traceId";
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
//        // 生成全局唯一的追踪ID
//        String traceId = UUID.randomUUID().toString().replace("-", "");
//
//        // 将追踪ID放入MDC中，便于日志追踪
//        MDC.put(TRACE_ID, traceId);
//
//        // 在响应头中添加追踪ID，便于客户端追踪请求
//        return chain.filter(exchange)
//                .then(Mono.fromRunnable(() -> {
//                    // 清理MDC中的追踪ID
//                    MDC.remove(TRACE_ID);
//                }))
//                .doOnTerminate(() -> {
//                    // 确保在任何情况下都清理MDC
//                    MDC.remove(TRACE_ID);
//                }).then();
//    }
//
//    @Override
//    public int getOrder() {
//        return Ordered.HIGHEST_PRECEDENCE + 1; // 略低于LoggingFilter的优先级
//    }
//}