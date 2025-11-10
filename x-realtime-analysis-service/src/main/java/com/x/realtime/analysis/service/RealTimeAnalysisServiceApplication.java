package com.x.realtime.analysis.service;

import com.x.realtime.analysis.service.service.RealTimeAnalysisService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class RealTimeAnalysisServiceApplication {

    private final RealTimeAnalysisService realTimeAnalysisService;

    public RealTimeAnalysisServiceApplication(RealTimeAnalysisService realTimeAnalysisService) {
        this.realTimeAnalysisService = realTimeAnalysisService;
    }

    public static void main(String[] args) {
        SpringApplication.run(RealTimeAnalysisServiceApplication.class, args);
    }

    @PostConstruct
    public void startAnalysis() {
        // 启动实时数据分析
        realTimeAnalysisService.analyzeDeviceDataStream();
    }
}