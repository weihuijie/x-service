package com.x.data.collection.service.grpc;

import com.x.grpc.data.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class DataServiceImpl extends DataServiceGrpc.DataServiceImplBase {
    
    @Override
    public void sendToKafka(SendToKafkaRequest request, StreamObserver<SendToKafkaResponse> responseObserver) {
        try {
            DeviceData deviceData = request.getDeviceData();
            
            // 模拟发送数据到Kafka
            System.out.println("发送数据到Kafka: " + deviceData);
            
            SendToKafkaResponse response = SendToKafkaResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("数据已发送到Kafka")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            SendToKafkaResponse response = SendToKafkaResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("发送数据到Kafka失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void sendToRabbitMQ(SendToRabbitMQRequest request, StreamObserver<SendToRabbitMQResponse> responseObserver) {
        try {
            DeviceData deviceData = request.getDeviceData();
            
            // 模拟发送数据到RabbitMQ
            System.out.println("发送数据到RabbitMQ: " + deviceData);
            
            SendToRabbitMQResponse response = SendToRabbitMQResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("数据已发送到RabbitMQ")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            SendToRabbitMQResponse response = SendToRabbitMQResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("发送数据到RabbitMQ失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void storeToMinIO(StoreToMinIORequest request, StreamObserver<StoreToMinIOResponse> responseObserver) {
        try {
            DeviceData deviceData = request.getDeviceData();
            
            // 模拟存储数据到MinIO
            System.out.println("存储数据到MinIO: " + deviceData);
            
            String fileUrl = "http://localhost:9000/device-data/" + deviceData.getDeviceId() + "-" + System.currentTimeMillis() + ".json";
            
            StoreToMinIOResponse response = StoreToMinIOResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("数据已存储到MinIO")
                    .setFileUrl(fileUrl)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            StoreToMinIOResponse response = StoreToMinIOResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("存储数据到MinIO失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void processDataAsync(ProcessDataAsyncRequest request, StreamObserver<ProcessDataAsyncResponse> responseObserver) {
        try {
            DeviceData deviceData = request.getDeviceData();
            
            // 模拟异步处理数据
            System.out.println("异步处理数据: " + deviceData);
            
            String fileUrl = "http://localhost:9000/device-data/" + deviceData.getDeviceId() + "-" + System.currentTimeMillis() + ".json";
            
            ProcessDataAsyncResponse response = ProcessDataAsyncResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("数据已异步处理")
                    .setFileUrl(fileUrl)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ProcessDataAsyncResponse response = ProcessDataAsyncResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("异步处理数据失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}