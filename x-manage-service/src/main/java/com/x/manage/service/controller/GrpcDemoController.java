package com.x.manage.service.controller;

import com.x.grpc.device.*;
import com.x.manage.service.service.DeviceGrpcClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grpc/demo")
public class GrpcDemoController {
    
    @Autowired
    private DeviceGrpcClientService deviceGrpcClientService;
    
    @PostMapping("/device")
    public CreateDeviceResponse createDevice(@RequestBody CreateDeviceRequest request) {
        return deviceGrpcClientService.createDevice(request);
    }
    
    @GetMapping("/device/{id}")
    public GetDeviceResponse getDevice(@PathVariable long id) {
        GetDeviceRequest request = GetDeviceRequest.newBuilder().setId(id).build();
        return deviceGrpcClientService.getDevice(request);
    }
    
    @PutMapping("/device/{id}/status")
    public UpdateDeviceStatusResponse updateDeviceStatus(@PathVariable long id, @RequestParam String status) {
        UpdateDeviceStatusRequest request = UpdateDeviceStatusRequest.newBuilder()
                .setId(id)
                .setStatus(status)
                .build();
        return deviceGrpcClientService.updateDeviceStatus(request);
    }
    
    @DeleteMapping("/device/{id}")
    public DeleteDeviceResponse deleteDevice(@PathVariable long id) {
        DeleteDeviceRequest request = DeleteDeviceRequest.newBuilder().setId(id).build();
        return deviceGrpcClientService.deleteDevice(request);
    }
    
    @GetMapping("/devices")
    public ListDevicesResponse listDevices(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        ListDevicesRequest request = ListDevicesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();
        return deviceGrpcClientService.listDevices(request);
    }
}