package com.x.repository.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeviceMapper {
    
    @Select("SELECT COUNT(*) FROM device")
    int getDeviceCount();
    
    @Select("SELECT id, name, status FROM device WHERE id = #{id}")
    Device getDeviceById(Long id);
    
    public static class Device {
        private Long id;
        private String name;
        private String status;
        
        // Getters and setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}