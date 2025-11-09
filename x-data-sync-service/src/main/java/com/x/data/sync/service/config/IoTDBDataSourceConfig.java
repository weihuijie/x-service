package com.x.data.sync.service.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * IoTDB 从数据源配置（无需@Primary，与MySQL数据源区分）
 */
@Slf4j
@Configuration
public class IoTDBDataSourceConfig {

    /**
     * 1. 创建IoTDB数据源（绑定application.yml中spring.datasource.iotdb前缀的配置）
     */
    @Bean(name = "iotdb")
    @ConfigurationProperties(prefix = "spring.datasource.iotdb")
    public DataSource iotdbDataSource() {
        return new HikariDataSource();
    }

    // 初始化 IOTDB 专属 JdbcTemplate
    @Bean(name = "iotdbJdbcTemplate")
    public JdbcTemplate iotdbJdbcTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("iotdb") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }
}