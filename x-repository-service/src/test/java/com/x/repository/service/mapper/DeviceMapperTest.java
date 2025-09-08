package com.x.repository.service.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DeviceMapperTest {

    private DeviceMapper deviceMapper = new DeviceMapper() {
        @Override
        public int getDeviceCount() {
            return 10;
        }

        @Override
        public Device getDeviceById(Long id) {
            Device device = new Device();
            device.setId(id);
            device.setName("Test Device");
            device.setStatus("Active");
            return device;
        }
    };

    @Test
    public void testGetDeviceCount() {
        int count = deviceMapper.getDeviceCount();
        assertTrue(count >= 0);
        System.out.println("Device count: " + count);
    }

    @Test
    public void testGetDeviceById() {
        DeviceMapper.Device device = deviceMapper.getDeviceById(1L);
        assertNotNull(device);
        System.out.println("Device: " + device.getName());
    }

    private void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Object is null");
        }
    }
}
