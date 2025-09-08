package com.x.common.component;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * MinIO组件工具类
 * 提供MinIO对象存储常用操作的封装
 */
@Component
public class MinIOComponentUtil {
    
    @Autowired
    private MinioClient minioClient;
    
    /**
     * 检查存储桶是否存在
     * @param bucketName 存储桶名称
     * @return 是否存在
     * @throws Exception 异常
     */
    public boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }
    
    /**
     * 创建存储桶
     * @param bucketName 存储桶名称
     * @throws Exception 异常
     */
    public void makeBucket(String bucketName) throws Exception {
        boolean exists = bucketExists(bucketName);
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
    
    /**
     * 上传对象
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param stream 输入流
     * @param contentType 内容类型
     * @throws Exception 异常
     */
    public void putObject(String bucketName, String objectName, InputStream stream, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream, stream.available(), -1)
                        .contentType(contentType)
                        .build());
    }
    
    /**
     * 上传对象（字节数组）
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param bytes 字节数组
     * @param contentType 内容类型
     * @throws Exception 异常
     */
    public void putObject(String bucketName, String objectName, byte[] bytes, String contentType) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(bais, bytes.length, -1)
                            .contentType(contentType)
                            .build());
        }
    }
    
    /**
     * 下载对象
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 对象输入流
     * @throws Exception 异常
     */
    public GetObjectResponse getObject(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
    
    /**
     * 删除对象
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @throws Exception 异常
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
    
    /**
     * 列出存储桶中的所有对象
     * @param bucketName 存储桶名称
     * @return 对象列表
     * @throws Exception 异常
     */
    public List<String> listObjects(String bucketName) throws Exception {
        List<String> objectNames = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .build());
        
        for (Result<Item> result : results) {
            Item item = result.get();
            objectNames.add(item.objectName());
        }
        
        return objectNames;
    }
    
    /**
     * 获取对象URL
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 对象URL
     * @throws Exception 异常
     */
    public String getPresignedObjectUrl(String bucketName, String objectName, int expiry) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expiry)
                        .build());
    }
}