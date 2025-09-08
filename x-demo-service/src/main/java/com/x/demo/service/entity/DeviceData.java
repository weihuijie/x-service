package com.x.demo.service.entity;

public class DeviceData {
    private String deviceId;
    private long timestamp;
    private String dataType;
    private String data;
    
    // Constructors
    public DeviceData() {}
    
    public DeviceData(String deviceId, long timestamp, String dataType, String data) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.dataType = dataType;
        this.data = data;
    }
    
    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "DeviceData{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", dataType='" + dataType + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}