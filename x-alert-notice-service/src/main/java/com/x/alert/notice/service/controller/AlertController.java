package com.x.alert.notice.service.controller;

import com.x.alert.notice.service.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // 发送告警通知
    @PostMapping("/send")
    public ResponseEntity<?> sendAlert(@RequestBody AlertService.AlertRequest request) {
        try {
            AlertService.AlertResult result = alertService.sendAlertByType(request);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            AlertService.AlertResult errorResult = new AlertService.AlertResult(false, "Error sending alert: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    // 获取告警统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getAlertStats() {
        AlertService.AlertStats stats = alertService.getAlertStats();
        return ResponseEntity.ok(stats);
    }
}
