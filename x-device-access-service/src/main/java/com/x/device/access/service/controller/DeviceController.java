package com.x.device.access.service.controller;

import com.x.device.access.service.grpc.DeviceServiceImpl;
import com.x.grpc.device.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/device")
public class DeviceController {
    
    @Autowired
    private DeviceServiceImpl deviceService;
    
    /**
     * 创建设备
     */
    @PostMapping
    public CreateDeviceResponseDTO createDevice(@RequestBody CreateDeviceRequest request) {
        // 创建一个StreamObserver来接收响应
        ResponseStreamObserver<CreateDeviceResponse> responseObserver = new ResponseStreamObserver<>();
        deviceService.createDevice(request, responseObserver);
        CreateDeviceResponse response = responseObserver.getResponse();
        return new CreateDeviceResponseDTO(response);
    }
    
    /**
     * 获取设备信息
     */
    @GetMapping("/{id}")
    public GetDeviceResponseDTO getDevice(@PathVariable long id) {
        GetDeviceRequest request = GetDeviceRequest.newBuilder().setId(id).build();
        ResponseStreamObserver<GetDeviceResponse> responseObserver = new ResponseStreamObserver<>();
        deviceService.getDevice(request, responseObserver);
        GetDeviceResponse response = responseObserver.getResponse();
        return new GetDeviceResponseDTO(response);
    }
    
    /**
     * 更新设备状态
     */
    @PutMapping("/{id}/status")
    public UpdateDeviceStatusResponseDTO updateDeviceStatus(@PathVariable long id, @RequestBody UpdateDeviceStatusRequest request) {
        UpdateDeviceStatusRequest updateRequest = request.toBuilder().setId(id).build();
        ResponseStreamObserver<UpdateDeviceStatusResponse> responseObserver = new ResponseStreamObserver<>();
        deviceService.updateDeviceStatus(updateRequest, responseObserver);
        UpdateDeviceStatusResponse response = responseObserver.getResponse();
        return new UpdateDeviceStatusResponseDTO(response);
    }
    
    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    public DeleteDeviceResponseDTO deleteDevice(@PathVariable long id) {
        DeleteDeviceRequest request = DeleteDeviceRequest.newBuilder().setId(id).build();
        ResponseStreamObserver<DeleteDeviceResponse> responseObserver = new ResponseStreamObserver<>();
        deviceService.deleteDevice(request, responseObserver);
        DeleteDeviceResponse response = responseObserver.getResponse();
        return new DeleteDeviceResponseDTO(response);
    }
    
    /**
     * 获取设备列表
     */
    @GetMapping("/list")
    public ListDevicesResponseDTO listDevices(@RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                          @RequestParam(value = "status", required = false) String status) {
        ListDevicesRequest.Builder builder = ListDevicesRequest.newBuilder()
                .setPage(page)
                .setSize(size);
        if (status != null) {
            builder.setStatus(status);
        }
        ListDevicesRequest request = builder.build();
        ResponseStreamObserver<ListDevicesResponse> responseObserver = new ResponseStreamObserver<>();
        deviceService.listDevices(request, responseObserver);
        ListDevicesResponse response = responseObserver.getResponse();
        return new ListDevicesResponseDTO(response);
    }
    
    /**
     * 执行操作
     */
    @PostMapping("/execute")
    public ExecuteOperationResponseDTO executeOperation(@RequestBody Map<String, String> params) {
        ExecuteOperationRequest.Builder builder = ExecuteOperationRequest.newBuilder();
        builder.putAllParams(params);
        ExecuteOperationRequest request = builder.build();
        ResponseStreamObserver<ExecuteOperationResponse> responseObserver = new ResponseStreamObserver<>();
        deviceService.executeOperation(request, responseObserver);
        ExecuteOperationResponse response = responseObserver.getResponse();
        return new ExecuteOperationResponseDTO(response);
    }
    
    /**
     * 简单的StreamObserver实现，用于捕获gRPC响应
     */
    private static class ResponseStreamObserver<T> implements io.grpc.stub.StreamObserver<T> {
        private T response;
        private Throwable error;
        
        @Override
        public void onNext(T value) {
            this.response = value;
        }
        
        @Override
        public void onError(Throwable t) {
            this.error = t;
        }
        
        @Override
        public void onCompleted() {
            // 不需要特殊处理
        }
        
        public T getResponse() {
            if (error != null) {
                throw new RuntimeException(error);
            }
            return response;
        }
    }
    
    // DTO类定义
    public static class CreateDeviceResponseDTO {
        public boolean success;
        public String message;
        public DeviceDTO device;
        
        public CreateDeviceResponseDTO(CreateDeviceResponse response) {
            this.success = response.getSuccess();
            this.message = response.getMessage();
            this.device = response.hasDevice() ? new DeviceDTO(response.getDevice()) : null;
        }
    }
    
    public static class GetDeviceResponseDTO {
        public boolean success;
        public String message;
        public DeviceDTO device;
        
        public GetDeviceResponseDTO(GetDeviceResponse response) {
            this.success = response.getSuccess();
            this.message = response.getMessage();
            this.device = response.hasDevice() ? new DeviceDTO(response.getDevice()) : null;
        }
    }
    
    public static class UpdateDeviceStatusResponseDTO {
        public boolean success;
        public String message;
        public DeviceDTO device;
        
        public UpdateDeviceStatusResponseDTO(UpdateDeviceStatusResponse response) {
            this.success = response.getSuccess();
            this.message = response.getMessage();
            this.device = response.hasDevice() ? new DeviceDTO(response.getDevice()) : null;
        }
    }
    
    public static class DeleteDeviceResponseDTO {
        public boolean success;
        public String message;
        
        public DeleteDeviceResponseDTO(DeleteDeviceResponse response) {
            this.success = response.getSuccess();
            this.message = response.getMessage();
        }
    }
    
    public static class ListDevicesResponseDTO {
        public boolean success;
        public String message;
        public List<DeviceDTO> devices;
        public int total;
        
        public ListDevicesResponseDTO(ListDevicesResponse response) {
            this.success = response.getSuccess();
            this.message = response.getMessage();
            this.devices = response.getDevicesList().stream().map(DeviceDTO::new).collect(Collectors.toList());
            this.total = response.getTotal();
        }
    }
    
    public static class ExecuteOperationResponseDTO {
        public boolean success;
        public int code;
        public String message;
        public Map<String, String> data;
        
        public ExecuteOperationResponseDTO(ExecuteOperationResponse response) {
            this.success = response.getSuccess();
            this.code = response.getCode();
            this.message = response.getMessage();
            this.data = response.getDataMap();
        }
    }
    
    public static class DeviceDTO {
        public long id;
        public String name;
        public String model;
        public String status;
        public String location;
        public long createTime;
        public long updateTime;
        
        public DeviceDTO(Device device) {
            this.id = device.getId();
            this.name = device.getName();
            this.model = device.getModel();
            this.status = device.getStatus();
            this.location = device.getLocation();
            this.createTime = device.getCreateTime();
            this.updateTime = device.getUpdateTime();
        }
    }
}