package com.x.config.center.service.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NacosConfigManagerTest {
    
    private NacosConfigManager configManager = new NacosConfigManager();
    
    @Test
    public void testGetConfig() {
        String dataId = "testDataId";
        String group = "testGroup";
        
        String config = configManager.getConfig(dataId, group);
        assertNotNull(config);
        System.out.println("Retrieved config: " + config);
    }
    
    @Test
    public void testPublishConfig() {
        String dataId = "testDataId";
        String group = "testGroup";
        String content = "testContent";
        
        boolean result = configManager.publishConfig(dataId, group, content);
        assertTrue(result);
    }
    
    @Test
    public void testRemoveConfig() {
        String dataId = "testDataId";
        String group = "testGroup";
        
        boolean result = configManager.removeConfig(dataId, group);
        assertTrue(result);
    }
}