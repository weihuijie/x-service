package com.x.alert.notice.service.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AlertService {
    
    // 模拟存储告警统计信息
    private final ConcurrentHashMap<String, AtomicLong> alertCountByType = new ConcurrentHashMap<>();
    private final AtomicLong totalAlertsSent = new AtomicLong(0);
    
    /**
     * 发送邮件通知
     */
    public AlertResult sendEmailAlert(String emailAddress, String subject, String message) {
        // 实际项目中这里会发送邮件
        System.out.println("Sending email to " + emailAddress + " with subject: " + subject + " and message: " + message);
        
        // 更新统计信息
        alertCountByType.computeIfAbsent("email", k -> new AtomicLong(0)).incrementAndGet();
        totalAlertsSent.incrementAndGet();
        
        return new AlertResult(true, "Email sent successfully");
    }
    
    /**
     * 发送短信通知
     */
    public AlertResult sendSmsAlert(String phoneNumber, String message) {
        // 实际项目中这里会发送短信
        System.out.println("Sending SMS to " + phoneNumber + " with message: " + message);
        
        // 更新统计信息
        alertCountByType.computeIfAbsent("sms", k -> new AtomicLong(0)).incrementAndGet();
        totalAlertsSent.incrementAndGet();
        
        return new AlertResult(true, "SMS sent successfully");
    }
    
    /**
     * 发送微信通知
     */
    public AlertResult sendWechatAlert(String wechatId, String message) {
        // 实际项目中这里会发送微信消息
        System.out.println("Sending WeChat message to " + wechatId + " with message: " + message);
        
        // 更新统计信息
        alertCountByType.computeIfAbsent("wechat", k -> new AtomicLong(0)).incrementAndGet();
        totalAlertsSent.incrementAndGet();
        
        return new AlertResult(true, "WeChat message sent successfully");
    }
    
    /**
     * 根据告警类型发送通知
     */
    public AlertResult sendAlertByType(AlertRequest request) {
        switch (request.getType().toLowerCase()) {
            case "email":
                return sendEmailAlert(request.getRecipient(), request.getSubject(), request.getMessage());
            case "sms":
                return sendSmsAlert(request.getRecipient(), request.getMessage());
            case "wechat":
                return sendWechatAlert(request.getRecipient(), request.getMessage());
            default:
                return new AlertResult(false, "Unsupported alert type: " + request.getType());
        }
    }
    
    /**
     * 获取告警统计信息
     */
    public AlertStats getAlertStats() {
        AlertStats stats = new AlertStats();
        stats.setTotalAlertsSent(totalAlertsSent.get());
        stats.setAlertCountByType(alertCountByType);
        return stats;
    }
    
    /**
     * 告警请求类
     */
    public static class AlertRequest {
        private String type;       // 告警类型: email, sms, wechat
        private String recipient;  // 接收者
        private String subject;    // 邮件主题（仅对邮件有效）
        private String message;    // 告警消息
        
        // Getters and setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getRecipient() {
            return recipient;
        }
        
        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    /**
     * 告警结果类
     */
    public static class AlertResult {
        private boolean success;
        private String message;
        
        public AlertResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters and setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    /**
     * 告警统计类
     */
    public static class AlertStats {
        private long totalAlertsSent;
        private ConcurrentHashMap<String, AtomicLong> alertCountByType;
        
        // Getters and setters
        public long getTotalAlertsSent() {
            return totalAlertsSent;
        }
        
        public void setTotalAlertsSent(long totalAlertsSent) {
            this.totalAlertsSent = totalAlertsSent;
        }
        
        public ConcurrentHashMap<String, AtomicLong> getAlertCountByType() {
            return alertCountByType;
        }
        
        public void setAlertCountByType(ConcurrentHashMap<String, AtomicLong> alertCountByType) {
            this.alertCountByType = alertCountByType;
        }
    }
}