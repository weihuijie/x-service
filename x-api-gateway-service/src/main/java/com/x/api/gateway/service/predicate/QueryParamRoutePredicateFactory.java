package com.x.api.gateway.service.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 自定义请求参数断言工厂
 * 根据请求参数进行路由判断
 */
@Component
public class QueryParamRoutePredicateFactory extends AbstractRoutePredicateFactory<QueryParamRoutePredicateFactory.Config> {

    public static final String PARAM_NAME_KEY = "paramName";
    public static final String PARAM_VALUE_KEY = "paramValue";
    public static final String REQUIRED_KEY = "required";

    public QueryParamRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(PARAM_NAME_KEY, PARAM_VALUE_KEY, REQUIRED_KEY);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
                
                // 检查参数是否存在
                boolean paramExists = queryParams.containsKey(config.getParamName());
                
                // 如果参数不是必须的，且不存在，则返回true
                if (!config.isRequired() && !paramExists) {
                    return true;
                }
                
                // 如果参数必须存在，但不存在，则返回false
                if (config.isRequired() && !paramExists) {
                    return false;
                }
                
                // 如果参数存在但没有指定值，则返回true
                if (config.getParamValue() == null || config.getParamValue().isEmpty()) {
                    return true;
                }
                
                // 检查参数值是否匹配
                List<String> values = queryParams.get(config.getParamName());
                return values != null && values.contains(config.getParamValue());
            }

            @Override
            public String toString() {
                return String.format("QueryParam: name=%s, value=%s, required=%s", 
                        config.getParamName(), config.getParamValue(), config.isRequired());
            }
        };
    }

    /**
     * 配置类
     */
    public static class Config {
        private String paramName;
        private String paramValue;
        private boolean required = true;

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getParamValue() {
            return paramValue;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}