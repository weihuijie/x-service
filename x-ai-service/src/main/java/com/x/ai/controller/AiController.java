//package com.x.ai.controller;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/ai")
//public class AiController {
//
//    private final ChatClient chatClient;
//
//    public AiController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .build();
//    }
//
//    @GetMapping("/chat")
//    public String chat(@RequestParam(value = "message", defaultValue = "你好，你是谁？") String message) {
//        return chatClient.prompt()
//                .user(message)
//                .call()
//                .content();
//    }
//
//    @GetMapping("/poem")
//    public String generatePoem(@RequestParam(value = "topic", defaultValue = "春天") String topic) {
//        return chatClient.prompt()
//                .user("写一首关于'{topic}'的诗，不超过4行")
//                .call()
//                .content();
//    }
//}