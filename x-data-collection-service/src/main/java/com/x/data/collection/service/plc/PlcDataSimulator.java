package com.x.data.collection.service.plc;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * PLC数据模拟器
 */
@Component
public class PlcDataSimulator {
    
    private final Random random = new Random();
    
    /**
     * 模拟从PLC读取数据
     * @param plcCode PLC代码
     * @return 模拟的PLC数据
     */
    public Map<String, Object> readPlcData(String plcCode) {
        Map<String, Object> plcData = new HashMap<>();
        
        // 模拟不同的PLC数据
        if (plcCode != null && !plcCode.isEmpty()) {
            plcData.put("plcCode", plcCode);
            plcData.put("temperature", 20 + random.nextDouble() * 30); // 温度 20-50度
            plcData.put("pressure", 1 + random.nextDouble() * 9); // 压力 1-10MPa
            plcData.put("flowRate", 100 + random.nextDouble() * 400); // 流量 100-500 m³/h
            plcData.put("timestamp", System.currentTimeMillis());
            plcData.put("status", "NORMAL");
        } else {
            plcData.put("error", "Invalid PLC code");
            plcData.put("status", "ERROR");
        }
        
        return plcData;
    }
}