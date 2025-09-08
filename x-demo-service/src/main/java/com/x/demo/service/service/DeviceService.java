package com.x.demo.service.service;

import com.x.demo.service.entity.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DeviceService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private MongoClient mongoClient;
    
    // MySQL操作 - 保存设备信息
    public Device saveDevice(Device device) {
        String sql = "INSERT INTO device (name, model, status, location) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, device.getName(), device.getModel(), device.getStatus(), device.getLocation());
        
        // 查询刚插入的设备ID
        String selectSql = "SELECT id FROM device WHERE name = ? AND model = ?";
        Long id = jdbcTemplate.queryForObject(selectSql, Long.class, device.getName(), device.getModel());
        device.setId(id);
        
        // 同步到Redis缓存
        redisTemplate.opsForValue().set("device:" + id, device, 30, TimeUnit.MINUTES);
        
        // 记录到MongoDB日志
        MongoDatabase database = mongoClient.getDatabase("demo_db");
        MongoCollection<Document> collection = database.getCollection("device_logs");
        
        Document logDocument = new Document()
                .append("deviceId", device.getId())
                .append("action", "CREATE")
                .append("timestamp", System.currentTimeMillis())
                .append("deviceInfo", device.toString());
        
        collection.insertOne(logDocument);
        
        return device;
    }
    
    // MySQL操作 - 获取设备信息
    public Device getDeviceById(Long id) {
        // 首先尝试从Redis获取
        Device cachedDevice = (Device) redisTemplate.opsForValue().get("device:" + id);
        if (cachedDevice != null) {
            System.out.println("从Redis缓存获取设备信息");
            return cachedDevice;
        }
        
        // Redis中没有则从MySQL获取
        String sql = "SELECT id, name, model, status, location FROM device WHERE id = ?";
        Device device = jdbcTemplate.queryForObject(sql, this::mapRowToDevice, id);
        
        // 同步到Redis缓存
        if (device != null) {
            redisTemplate.opsForValue().set("device:" + id, device, 30, TimeUnit.MINUTES);
        }
        
        return device;
    }
    
    // MySQL操作 - 获取所有设备
    public List<Device> getAllDevices() {
        String sql = "SELECT id, name, model, status, location FROM device";
        return jdbcTemplate.query(sql, this::mapRowToDevice);
    }
    
    // 更新设备状态
    public Device updateDeviceStatus(Long id, String status) {
        String sql = "UPDATE device SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
        
        // 清除Redis缓存
        redisTemplate.delete("device:" + id);
        
        // 记录到MongoDB日志
        MongoDatabase database = mongoClient.getDatabase("demo_db");
        MongoCollection<Document> collection = database.getCollection("device_logs");
        
        Document logDocument = new Document()
                .append("deviceId", id)
                .append("action", "UPDATE_STATUS")
                .append("newStatus", status)
                .append("timestamp", System.currentTimeMillis());
        
        collection.insertOne(logDocument);
        
        return getDeviceById(id);
    }
    
    // 删除设备
    public void deleteDevice(Long id) {
        String sql = "DELETE FROM device WHERE id = ?";
        jdbcTemplate.update(sql, id);
        
        // 清除Redis缓存
        redisTemplate.delete("device:" + id);
        
        // 记录到MongoDB日志
        MongoDatabase database = mongoClient.getDatabase("demo_db");
        MongoCollection<Document> collection = database.getCollection("device_logs");
        
        Document logDocument = new Document()
                .append("deviceId", id)
                .append("action", "DELETE")
                .append("timestamp", System.currentTimeMillis());
        
        collection.insertOne(logDocument);
    }
    
    // 映射ResultSet到Device对象
    private Device mapRowToDevice(ResultSet rs, int rowNum) throws SQLException {
        Device device = new Device();
        device.setId(rs.getLong("id"));
        device.setName(rs.getString("name"));
        device.setModel(rs.getString("model"));
        device.setStatus(rs.getString("status"));
        device.setLocation(rs.getString("location"));
        return device;
    }
}