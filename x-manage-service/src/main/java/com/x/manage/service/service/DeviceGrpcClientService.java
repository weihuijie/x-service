package com.x.manage.service.service;

import com.x.grpc.device.*;
import io.grpc.ManagedChannel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class DeviceGrpcClientService {
    
    @GrpcClient("device-service")
    private ManagedChannel deviceServiceChannel;
    
    public CreateDeviceResponse createDevice(CreateDeviceRequest request) {
        DeviceServiceGrpc.DeviceServiceBlockingStub stub = DeviceServiceGrpc.newBlockingStub(deviceServiceChannel);
        return stub.createDevice(request);
    }
    
    public GetDeviceResponse getDevice(GetDeviceRequest request) {
        DeviceServiceGrpc.DeviceServiceBlockingStub stub = DeviceServiceGrpc.newBlockingStub(deviceServiceChannel);
        return stub.getDevice(request);
    }
    
    public UpdateDeviceStatusResponse updateDeviceStatus(UpdateDeviceStatusRequest request) {
        DeviceServiceGrpc.DeviceServiceBlockingStub stub = DeviceServiceGrpc.newBlockingStub(deviceServiceChannel);
        return stub.updateDeviceStatus(request);
    }
    
    public DeleteDeviceResponse deleteDevice(DeleteDeviceRequest request) {
        DeviceServiceGrpc.DeviceServiceBlockingStub stub = DeviceServiceGrpc.newBlockingStub(deviceServiceChannel);
        return stub.deleteDevice(request);
    }
    
    public ListDevicesResponse listDevices(ListDevicesRequest request) {
        DeviceServiceGrpc.DeviceServiceBlockingStub stub = DeviceServiceGrpc.newBlockingStub(deviceServiceChannel);
        return stub.listDevices(request);
    }
}