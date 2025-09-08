package com.x.manage.service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DeviceControllerTest {

    private DeviceController deviceController = new DeviceController();

    @Test
    public void testGetDeviceList() {
        String result = deviceController.getDeviceList().toString();
        assertNotNull(result);
        System.out.println("Device list: " + result);
    }

    @Test
    public void testGetDeviceDetail() {
        String result = String.valueOf(deviceController.getDeviceDetail(1L));
        assertNotNull(result);
        System.out.println("Device detail: " + result);
    }
}
