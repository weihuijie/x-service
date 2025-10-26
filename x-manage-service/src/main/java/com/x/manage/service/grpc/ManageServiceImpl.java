package com.x.manage.service.grpc;

import com.x.grpc.manage.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@GrpcService
public class ManageServiceImpl extends ManageServiceGrpc.ManageServiceImplBase {
    
    private static final Logger logger = LoggerFactory.getLogger(ManageServiceImpl.class);
    
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
                case "test":
                    success = handleTestOperation(params, resultData);
                    message = success ? "测试操作成功" : "测试操作失败";
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
            logger.error("执行操作失败: {}", e.getMessage(), e);
            ExecuteOperationResponse response = ExecuteOperationResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("操作执行失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    private boolean handleTestOperation(Map<String, String> params, Map<String, String> resultData) {
        resultData.put("message", "这是一个测试操作");
        resultData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return true;
    }
}