package com.x.api.gateway.service.client;

import com.x.grpc.device.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Dubbo设备服务客户端 - 使用Dubbo调用设备管理服务的gRPC接口
 */
@Component
public class DubboDeviceServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DubboDeviceServiceClient.class);

    // 完善DubboReference配置，添加timeout、retries、cluster和loadbalance等参数
    @DubboReference(
            version = "1.0.0",
            check = false,
            timeout = 5000,            // 超时时间5秒，比认证服务稍长
            retries = 0,               // 不重试，避免重复操作
            cluster = "failfast",      // 快速失败
            loadbalance = "leastactive" // 最小活跃数负载均衡
    )
    private DubboDeviceServiceGrpc.IDeviceService deviceService;

    /**
     * 创建设备
     * @param name 设备名称
     * @param model 设备型号
     * @param status 设备状态
     * @param location 设备位置
     * @return 创建结果
     */
    public CreateDeviceResponse createDevice(String name, String model, String status, String location) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling device service createDevice: {}", name);
                    CreateDeviceRequest request = CreateDeviceRequest.newBuilder()
                            .setName(name)
                            .setModel(model)
                            .setStatus(status)
                            .setLocation(location)
                            .build();
                    return deviceService.createDevice(request);
                },
                "createDevice",
                () -> CreateDeviceResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to create device")
                        .build()
        );
    }

    /**
     * 获取设备信息
     * @param id 设备ID
     * @return 设备信息
     */
    public GetDeviceResponse getDevice(long id) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling device service getDevice: {}", id);
                    GetDeviceRequest request = GetDeviceRequest.newBuilder()
                            .setId(id)
                            .build();
                    return deviceService.getDevice(request);
                },
                "getDevice",
                () -> GetDeviceResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to get device")
                        .build()
        );
    }

    /**
     * 更新设备状态
     * @param id 设备ID
     * @param status 新状态
     * @return 更新结果
     */
    public UpdateDeviceStatusResponse updateDeviceStatus(long id, String status) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling device service updateDeviceStatus: {}, {}", id, status);
                    UpdateDeviceStatusRequest request = UpdateDeviceStatusRequest.newBuilder()
                            .setId(id)
                            .setStatus(status)
                            .build();
                    return deviceService.updateDeviceStatus(request);
                },
                "updateDeviceStatus",
                () -> UpdateDeviceStatusResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to update device status")
                        .build()
        );
    }

    /**
     * 删除设备
     * @param id 设备ID
     * @return 删除结果
     */
    public DeleteDeviceResponse deleteDevice(long id) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling device service deleteDevice: {}", id);
                    DeleteDeviceRequest request = DeleteDeviceRequest.newBuilder()
                            .setId(id)
                            .build();
                    return deviceService.deleteDevice(request);
                },
                "deleteDevice",
                () -> DeleteDeviceResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to delete device")
                        .build()
        );
    }

    /**
     * 获取设备列表
     * @param page 页码
     * @param size 每页大小
     * @param status 设备状态(可选)
     * @return 设备列表
     */
    public ListDevicesResponse listDevices(int page, int size, String status) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling device service listDevices: page={}, size={}, status={}", page, size, status);
                    ListDevicesRequest.Builder requestBuilder = ListDevicesRequest.newBuilder()
                            .setPage(page)
                            .setSize(size);
                    
                    if (status != null && !status.isEmpty()) {
                        requestBuilder.setStatus(status);
                    }
                    
                    return deviceService.listDevices(requestBuilder.build());
                },
                "listDevices",
                () -> ListDevicesResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to list devices")
                        .build()
        );
    }

    // 通用的Dubbo服务调用与错误处理
    private <T> T invokeWithErrorHandling(
            DubboServiceCall<T> call,
            String operation,
            FallbackProvider<T> fallbackProvider) {
        
        // 简单的服务熔断判断 - 实际项目中可以集成Sentinel或Resilience4j
        // 注意：在新版本Dubbo中，RpcStatus的API已更改，此处暂时移除相关逻辑
        // 可以在后续版本中使用Sentinel等组件替代熔断功能
        
        try {
            return call.call();
        } catch (Exception e) {
            logger.error("Failed to call device service {}: {}", operation, e.getMessage(), e);
            return fallbackProvider.getFallback();
        }
    }

    // 函数式接口定义
    private interface DubboServiceCall<T> {
        T call();
    }

    private interface FallbackProvider<T> {
        T getFallback();
    }
}