package com.x.device.access.service.grpc;

import com.x.grpc.device.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.x.device.access.service.handler.DeviceAccessHandler;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@GrpcService
@Service
public class DeviceServiceImpl extends DeviceServiceGrpc.DeviceServiceImplBase {
    
    private final DeviceAccessHandler deviceHandler = new DeviceAccessHandler();
    private final Map<Long, Device> deviceStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public void createDevice(CreateDeviceRequest request, StreamObserver<CreateDeviceResponse> responseObserver) {
        try {
            long id = idGenerator.getAndIncrement();
            long currentTime = System.currentTimeMillis();
            
            Device device = Device.newBuilder()
                    .setId(id)
                    .setName(request.getName())
                    .setModel(request.getModel())
                    .setStatus(request.getStatus())
                    .setLocation(request.getLocation())
                    .setCreateTime(currentTime)
                    .setUpdateTime(currentTime)
                    .build();
            
            deviceStore.put(id, device);
            
            // 调用设备处理逻辑
            deviceHandler.handleDeviceConnect(String.valueOf(id));
            
            CreateDeviceResponse response = CreateDeviceResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("设备创建成功")
                    .setDevice(device)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            CreateDeviceResponse response = CreateDeviceResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("设备创建失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void getDevice(GetDeviceRequest request, StreamObserver<GetDeviceResponse> responseObserver) {
        try {
            long id = request.getId();
            Device device = deviceStore.get(id);
            
            if (device != null) {
                GetDeviceResponse response = GetDeviceResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("获取设备成功")
                        .setDevice(device)
                        .build();
                
                responseObserver.onNext(response);
            } else {
                GetDeviceResponse response = GetDeviceResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("设备不存在")
                        .build();
                
                responseObserver.onNext(response);
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            GetDeviceResponse response = GetDeviceResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("获取设备失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void updateDeviceStatus(UpdateDeviceStatusRequest request, StreamObserver<UpdateDeviceStatusResponse> responseObserver) {
        try {
            long id = request.getId();
            Device device = deviceStore.get(id);
            
            if (device != null) {
                Device updatedDevice = device.toBuilder()
                        .setStatus(request.getStatus())
                        .setUpdateTime(System.currentTimeMillis())
                        .build();
                
                deviceStore.put(id, updatedDevice);
                
                UpdateDeviceStatusResponse response = UpdateDeviceStatusResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("设备状态更新成功")
                        .setDevice(updatedDevice)
                        .build();
                
                responseObserver.onNext(response);
            } else {
                UpdateDeviceStatusResponse response = UpdateDeviceStatusResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("设备不存在")
                        .build();
                
                responseObserver.onNext(response);
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            UpdateDeviceStatusResponse response = UpdateDeviceStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("设备状态更新失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void deleteDevice(DeleteDeviceRequest request, StreamObserver<DeleteDeviceResponse> responseObserver) {
        try {
            long id = request.getId();
            Device device = deviceStore.remove(id);
            
            if (device != null) {
                // 调用设备处理逻辑
                deviceHandler.handleDeviceDisconnect(String.valueOf(id));
                
                DeleteDeviceResponse response = DeleteDeviceResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("设备删除成功")
                        .build();
                
                responseObserver.onNext(response);
            } else {
                DeleteDeviceResponse response = DeleteDeviceResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("设备不存在")
                        .build();
                
                responseObserver.onNext(response);
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            DeleteDeviceResponse response = DeleteDeviceResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("设备删除失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void listDevices(ListDevicesRequest request, StreamObserver<ListDevicesResponse> responseObserver) {
        try {
            int page = request.getPage();
            int size = request.getSize();
            String status = request.getStatus();
            
            // 简单分页实现
            Device[] devices = deviceStore.values().stream()
                    .filter(device -> status == null || status.isEmpty() || device.getStatus().equals(status))
                    .skip((long) (page - 1) * size)
                    .limit(size)
                    .toArray(Device[]::new);
            
            ListDevicesResponse response = ListDevicesResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("获取设备列表成功")
                    .addAllDevices(List.of(devices))
                    .setTotal(deviceStore.size())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ListDevicesResponse response = ListDevicesResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("获取设备列表失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void executeOperation(ExecuteOperationRequest request, StreamObserver<ExecuteOperationResponse> responseObserver) {
        try {
            String operation = request.getOperation();
            Map<String, String> params = request.getParamsMap();
            
            Map<String, String> resultData = new HashMap<>();
            boolean success = true;
            String message = "操作成功";
            int code = 200;
            
            switch (operation.toLowerCase()) {
                case "create":
                    success = handleCreateDeviceOperation(params, resultData);
                    message = success ? "设备创建成功" : "设备创建失败";
                    break;
                case "get":
                    success = handleGetDeviceOperation(params, resultData);
                    message = success ? "获取设备成功" : "获取设备失败";
                    break;
                case "list":
                    success = handleListDevicesOperation(params, resultData);
                    message = success ? "获取设备列表成功" : "获取设备列表失败";
                    break;
                case "updatestatus":
                    success = handleUpdateDeviceStatusOperation(params, resultData);
                    message = success ? "设备状态更新成功" : "设备状态更新失败";
                    break;
                case "delete":
                    success = handleDeleteDeviceOperation(params, resultData);
                    message = success ? "设备删除成功" : "设备删除失败";
                    break;
                default:
                    success = false;
                    message = "不支持的操作: " + operation;
                    code = 400;
                    break;
            }
            
            ExecuteOperationResponse response = ExecuteOperationResponse.newBuilder()
                    .setSuccess(success)
                    .setCode(code)
                    .setMessage(message)
                    .putAllData(resultData)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ExecuteOperationResponse response = ExecuteOperationResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("操作执行失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    private boolean handleCreateDeviceOperation(Map<String, String> params, Map<String, String> resultData) {
        String name = params.get("name");
        String model = params.get("model");
        String status = params.get("status");
        String location = params.get("location");
        
        if (name == null || model == null || status == null || location == null) {
            resultData.put("error", "设备名称、型号、状态和位置不能为空");
            return false;
        }
        
        long id = idGenerator.getAndIncrement();
        long currentTime = System.currentTimeMillis();
        
        Device device = Device.newBuilder()
                .setId(id)
                .setName(name)
                .setModel(model)
                .setStatus(status)
                .setLocation(location)
                .setCreateTime(currentTime)
                .setUpdateTime(currentTime)
                .build();
        
        deviceStore.put(id, device);
        
        // 调用设备处理逻辑
        deviceHandler.handleDeviceConnect(String.valueOf(id));
        
        resultData.put("id", String.valueOf(id));
        resultData.put("name", name);
        resultData.put("model", model);
        resultData.put("status", status);
        resultData.put("location", location);
        return true;
    }
    
    private boolean handleGetDeviceOperation(Map<String, String> params, Map<String, String> resultData) {
        String idStr = params.get("id");
        if (idStr == null) {
            resultData.put("error", "设备ID不能为空");
            return false;
        }
        
        try {
            long id = Long.parseLong(idStr);
            Device device = deviceStore.get(id);
            
            if (device != null) {
                resultData.put("id", String.valueOf(device.getId()));
                resultData.put("name", device.getName());
                resultData.put("model", device.getModel());
                resultData.put("status", device.getStatus());
                resultData.put("location", device.getLocation());
                resultData.put("createTime", String.valueOf(device.getCreateTime()));
                resultData.put("updateTime", String.valueOf(device.getUpdateTime()));
                return true;
            } else {
                resultData.put("error", "设备不存在");
                return false;
            }
        } catch (NumberFormatException e) {
            resultData.put("error", "设备ID格式错误");
            return false;
        }
    }
    
    private boolean handleListDevicesOperation(Map<String, String> params, Map<String, String> resultData) {
        int page = Integer.parseInt(params.getOrDefault("page", "1"));
        int size = Integer.parseInt(params.getOrDefault("size", "10"));
        String status = params.get("status");
        
        // 简单分页实现
        Device[] devices = deviceStore.values().stream()
                .filter(device -> status == null || status.isEmpty() || device.getStatus().equals(status))
                .skip((long) (page - 1) * size)
                .limit(size)
                .toArray(Device[]::new);
        
        resultData.put("total", String.valueOf(deviceStore.size()));
        resultData.put("page", String.valueOf(page));
        resultData.put("size", String.valueOf(size));
        resultData.put("devicesCount", String.valueOf(devices.length));
        
        for (int i = 0; i < devices.length; i++) {
            Device device = devices[i];
            String prefix = "device_" + i + "_";
            resultData.put(prefix + "id", String.valueOf(device.getId()));
            resultData.put(prefix + "name", device.getName());
            resultData.put(prefix + "model", device.getModel());
            resultData.put(prefix + "status", device.getStatus());
            resultData.put(prefix + "location", device.getLocation());
        }
        
        return true;
    }
    
    private boolean handleUpdateDeviceStatusOperation(Map<String, String> params, Map<String, String> resultData) {
        String idStr = params.get("id");
        String status = params.get("status");
        
        if (idStr == null) {
            resultData.put("error", "设备ID不能为空");
            return false;
        }
        
        if (status == null) {
            resultData.put("error", "设备状态不能为空");
            return false;
        }
        
        try {
            long id = Long.parseLong(idStr);
            Device device = deviceStore.get(id);
            
            if (device != null) {
                Device updatedDevice = device.toBuilder()
                        .setStatus(status)
                        .setUpdateTime(System.currentTimeMillis())
                        .build();
                
                deviceStore.put(id, updatedDevice);
                resultData.put("id", String.valueOf(id));
                resultData.put("status", status);
                return true;
            } else {
                resultData.put("error", "设备不存在");
                return false;
            }
        } catch (NumberFormatException e) {
            resultData.put("error", "设备ID格式错误");
            return false;
        }
    }
    
    private boolean handleDeleteDeviceOperation(Map<String, String> params, Map<String, String> resultData) {
        String idStr = params.get("id");
        if (idStr == null) {
            resultData.put("error", "设备ID不能为空");
            return false;
        }
        
        try {
            long id = Long.parseLong(idStr);
            Device device = deviceStore.remove(id);
            
            if (device != null) {
                // 调用设备处理逻辑
                deviceHandler.handleDeviceDisconnect(String.valueOf(id));
                resultData.put("id", String.valueOf(id));
                return true;
            } else {
                resultData.put("error", "设备不存在");
                return false;
            }
        } catch (NumberFormatException e) {
            resultData.put("error", "设备ID格式错误");
            return false;
        }
    }
}