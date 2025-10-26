package com.x.device.access.service.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.Message;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class ProtobufHttpMessageConverter extends AbstractHttpMessageConverter<Message> {

    private final ObjectMapper objectMapper;

    public ProtobufHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 禁用FAIL_ON_EMPTY_BEANS特性，避免Protobuf类序列化问题
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Message.class.isAssignableFrom(clazz);
    }

    @Override
    protected Message readInternal(Class<? extends Message> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        // 由于Protobuf对象是不可变的，且没有默认构造函数，读取操作比较复杂，这里简化处理
        return null;
    }

    @Override
    protected void writeInternal(Message message, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        String jsonString = objectMapper.writeValueAsString(message);
        outputMessage.getBody().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_JSON);
    }
}