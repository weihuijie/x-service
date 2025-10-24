package com.x.api.gateway.service.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应格式过滤器 - 将所有响应包装为统一的JSON格式
 */
@Component
public class ResponseWrapperFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ResponseWrapperFilter.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        // 对于WebSocket等特殊请求不进行包装
        HttpMethod method = exchange.getRequest().getMethod();
        String upgradeHeader = exchange.getRequest().getHeaders().getFirst("Upgrade");
        if (HttpMethod.GET.equals(method) && "websocket".equalsIgnoreCase(upgradeHeader)) {
            return chain.filter(exchange);
        }

        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                if (body instanceof reactor.core.publisher.Flux) {
                    return super.writeWith(((reactor.core.publisher.Flux<DataBuffer>) body).map(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        String responseBody = new String(content, StandardCharsets.UTF_8);
                        
                        // 包装响应体
                        String wrappedResponse = wrapResponseBody(responseBody, (HttpStatus) this.getDelegate().getStatusCode());
                        
                        // 创建新的数据缓冲区
                        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                        return bufferFactory.wrap(wrappedResponse.getBytes(StandardCharsets.UTF_8));
                    }));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }

    /**
     * 包装响应体
     */
    private String wrapResponseBody(String originalBody, HttpStatus status) {
        try {
            Map<String, Object> responseWrapper = new HashMap<>();
            responseWrapper.put("code", status != null ? status.value() : 200);
            responseWrapper.put("message", status != null ? status.getReasonPhrase() : "OK");
            
            if (originalBody != null && !originalBody.isEmpty()) {
                // 尝试解析原始响应体为JSON对象
                try {
                    Object data = objectMapper.readValue(originalBody, Object.class);
                    responseWrapper.put("data", data);
                } catch (JsonProcessingException e) {
                    // 如果不是有效的JSON，将其作为字符串处理
                    responseWrapper.put("data", originalBody);
                }
            } else {
                responseWrapper.put("data", new HashMap<>());
            }
            
            responseWrapper.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(responseWrapper);
        } catch (Exception e) {
            logger.error("Error wrapping response body: {}", e.getMessage(), e);
            return originalBody; // 出错时返回原始响应
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10; // 在大多数过滤器之后执行
    }
}