package com.x.offline.analysis.service.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// Spark 配置类
@Component
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "spark")
@Data
public class SparkConfig {
    private String appName;
    private String master;
    private String deployMode;
    private String jars;
    private Map<String, String> executor = new HashMap<>();
    private Integer numExecutors;
}