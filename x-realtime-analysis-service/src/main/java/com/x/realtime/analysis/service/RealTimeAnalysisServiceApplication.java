package com.x.realtime.analysis.service;

import com.x.realtime.analysis.service.service.RealTimeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class RealTimeAnalysisServiceApplication {

    @Autowired
    private RealTimeAnalysisService realTimeAnalysisService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RealTimeAnalysisServiceApplication.class, args);
    }

    @PostConstruct
    public void startAnalysis() {
        // 启动实时数据分析
        realTimeAnalysisService.analyzeDeviceDataStream();
    }
}