package com.x.offline.analysis.service.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Hadoop 配置类
 *
 * @author whj
 */
@Component
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "hadoop")
@Data
public class HadoopConfig {
    private Map<String, String> hdfs = new HashMap<>();
    private Map<String, String> yarn = new HashMap<>();
}