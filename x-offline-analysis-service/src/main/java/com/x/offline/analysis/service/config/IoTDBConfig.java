package com.x.offline.analysis.service.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IoTDB 配置类
 *
 * @author whj
 */
@Component
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "iotdb")
@Data
public class IoTDBConfig {
    private String url;
    private String user;
    private String password;
    private String tablePath;
}
