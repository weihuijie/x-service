package com.x.data.sync.service.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * MySQL 主数据源配置（@Primary 标识主数据源，避免冲突）
 */
@Slf4j
@Configuration
public class MysqlDataSourceConfig {


    // 绑定 MySQL 数据源配置（spring.datasource.mysql），@Primary 指定为默认数据源
    @Primary
    @Bean(name = "mysql")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSource mysqlDataSource() {
        return new HikariDataSource();
    }

    // 初始化 MySQL 专属 JdbcTemplate（默认 JdbcTemplate，若不指定 Qualifier 则注入此对象）
    @Primary
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("mysql") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }
}