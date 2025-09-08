package com.x.demo.service.service;

import com.x.demo.service.entity.DeviceData;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DataProcessingService {
    
    private final RabbitTemplate rabbitTemplate;
    private final MinioClient minioClient;
    private final KafkaProducer<String, String> kafkaProducer;

    @Autowired
    public DataProcessingService(RabbitTemplate rabbitTemplate, MinioClient minioClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.minioClient = minioClient;

        // 初始化Kafka生产者
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.kafkaProducer = new KafkaProducer<>(props);
    }
    
    // 处理设备数据并发送到Kafka
    public void processDataWithKafka(DeviceData deviceData) {
        // 将设备数据发送到Kafka主题
        String topic = "device-data-topic";
        String key = deviceData.getDeviceId();
        String value = deviceData.toString();
        
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        kafkaProducer.send(record);
        
        System.out.println("设备数据已发送到Kafka: " + value);
    }
    
    // 处理设备数据并发送到RabbitMQ
    public void processDataWithRabbitMQ(DeviceData deviceData) {
        // 将设备数据发送到RabbitMQ交换机
        String exchange = "device-data-exchange";
        String routingKey = "device.data";
        rabbitTemplate.convertAndSend(exchange, routingKey, deviceData.toString());
        
        System.out.println("设备数据已发送到RabbitMQ: " + deviceData.toString());
    }
    
    // 存储设备数据到MinIO
    public String storeDataToMinIO(DeviceData deviceData) {
        try {
            // 生成唯一的文件名
            String fileName = "device-data-" + UUID.randomUUID().toString() + ".json";
            String bucketName = "device-data";
            
            // 创建bucket（如果不存在）
            boolean bucketExists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
            
            if (!bucketExists) {
                minioClient.makeBucket(
                    io.minio.MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
            
            // 将设备数据转换为JSON字符串
            String jsonData = "{\n" +
                    "  \"deviceId\": \"" + deviceData.getDeviceId() + "\",\n" +
                    "  \"timestamp\": " + deviceData.getTimestamp() + ",\n" +
                    "  \"dataType\": \"" + deviceData.getDataType() + "\",\n" +
                    "  \"data\": \"" + deviceData.getData() + "\"\n" +
                    "}";
            
            // 上传到MinIO
            ByteArrayInputStream bais = new ByteArrayInputStream(jsonData.getBytes("UTF-8"));
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(bais, jsonData.length(), -1)
                    .contentType("application/json")
                    .build()
            );
            
            String fileUrl = "http://localhost:9000/" + bucketName + "/" + fileName;
            System.out.println("设备数据已存储到MinIO: " + fileUrl);
            
            return fileUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 异步处理设备数据
    public CompletableFuture<String> processDeviceDataAsync(DeviceData deviceData) {
        return CompletableFuture.supplyAsync(() -> {
            // 发送到Kafka
            processDataWithKafka(deviceData);
            
            // 发送到RabbitMQ
            processDataWithRabbitMQ(deviceData);
            
            // 存储到MinIO
            return storeDataToMinIO(deviceData);
        });
    }
}