package com.x.file.service.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.x.file.service.utils.FileMagicNumberUtil;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件存储服务类
 *
 * @author whj
 */
@Slf4j
@Service
public class FileStorageService {

    // ==================== 常量定义（避免硬编码）====================
    /** 默认预签名URL有效期（30分钟） */
    private static final int DEFAULT_URL_EXPIRY_SECONDS = 30 * 60;
    /** 分片大小（5MB，MinIO最小分片1MB，建议5-10MB） */
    private static final long CHUNK_SIZE = 5 * 1024 * 1024;
    /** 分布式锁前缀 */
    private static final String LOCK_PREFIX = "file:upload:";
    /** 分片上传锁前缀（防止同一文件并发分片上传） */
    private static final String CHUNK_LOCK_PREFIX = "file:chunk:upload:";
    /** Redis 分片上传元数据前缀（跟踪分片状态） */
    private static final String MULTIPART_META_PREFIX = "multipart:upload:";
    /** 分片元数据过期时间（2小时，避免垃圾数据） */
    private static final long MULTIPART_META_EXPIRE_SECONDS = 7200;

    /** MinIO 存储原始文件名的元数据 Key */
    private static final String ORIGINAL_FILENAME_META_KEY = "original-filename";
    /** 文件名特殊字符替换规则（避免 MinIO 存储异常） */
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    // ==================== 依赖注入（新增 StringRedisTemplate 跟踪分片）====================
    private final MinioClient minioClient;
    private final DistributedLockService distributedLockService;
    private final SyncUploadTaskService syncUploadTaskService;
    private final WebClient webClient;
    private final Executor uploadTaskExecutor;
    private final Executor downloadTaskExecutor;
    private final StringRedisTemplate stringRedisTemplate;

    // ==================== 配置参数 ====================
    @Value("${minio.bucket-name:test}")
    private String bucketName;

    // ==================== 构造方法====================
    @Autowired
    public FileStorageService(MinioClient minioClient,
                              DistributedLockService distributedLockService,
                              SyncUploadTaskService syncUploadTaskService,
                              WebClient.Builder webClientBuilder,
                              StringRedisTemplate stringRedisTemplate,
                              @Value("${minio.bucket-name:test}") String bucketName,
                              @Qualifier("uploadTaskExecutor") Executor uploadTaskExecutor,
                              @Qualifier("downloadTaskExecutor") Executor downloadTaskExecutor) {
        this.minioClient = minioClient;
        this.distributedLockService = distributedLockService;
        this.syncUploadTaskService = syncUploadTaskService;
        this.webClient = webClientBuilder.build();
        this.stringRedisTemplate = stringRedisTemplate;
        this.bucketName = bucketName;
        this.uploadTaskExecutor = uploadTaskExecutor;
        this.downloadTaskExecutor = downloadTaskExecutor;
    }

    // ==================== 服务初始化（启动时创建存储桶，避免重复检查）====================
    @PostConstruct
    public void initMinioBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO存储桶创建成功：{}", bucketName);
            } else {
                log.info("MinIO存储桶已存在：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO存储桶初始化失败", e);
            throw new RuntimeException("文件存储服务启动失败：" + e.getMessage());
        }
    }

    // ==================== 核心方法 ====================

    /**
     * 单文件异步上传（支持防重复、MD5校验，优化流处理）
     * @param file 上传文件
     * @return 预签名访问URL
     */
    @Async("uploadTaskExecutor")
    public CompletableFuture<Map<String, String>> uploadFileAsync(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            Assert.notNull(file, "上传文件不能为空");
            String originalFilename = file.getOriginalFilename();
            Assert.hasText(originalFilename, "文件名不能为空");

            try (InputStream fileInputStream = file.getInputStream()) {
                // 计算文件MD5（用于重复上传判断）
                String fileMd5 = DigestUtils.md5DigestAsHex(fileInputStream);

                // 重新获取上传流
                try (InputStream uploadInputStream = file.getInputStream()) {
                    // 处理文件名（安全化+防覆盖）
                    String safeOriginalName = processSafeFilename(originalFilename);
                    String storageFileName = UUID.randomUUID().toString().replace("-", "") + "_" + safeOriginalName;

                    // 分布式锁（重试2次，间隔100ms）
                    String lockKey = LOCK_PREFIX + fileMd5;
                    String requestId = UUID.randomUUID().toString() + ":" + Thread.currentThread().getId();
                    boolean lockAcquired = distributedLockService.tryLock(lockKey, requestId, 30, 2, 100);

                    try {
                        if (!lockAcquired) {
                            // 锁获取失败：先检查文件是否已上传完成（第一个请求可能已结束）
                            String existingStorageFileName = getStorageFileNameByMd5(fileMd5);
                            if (StringUtils.containsWhitespace(existingStorageFileName)) {
                                log.info("文件已上传完成，直接复用结果：originalFilename={}, storageFileName={}",
                                        originalFilename, existingStorageFileName);
                                String fileUrl = getFileUrl(existingStorageFileName, DEFAULT_URL_EXPIRY_SECONDS);
                                return buildResultMap(originalFilename, existingStorageFileName, fileUrl, fileMd5);
                            }
                            // 未完成：返回重复提交提示（而非“稍后重试”）
                            throw new RuntimeException("文件正在上传中，请勿重复提交");
                        }

                        // 检查是否已存在相同文件（防重复上传）
                        String existingStorageFileName = getStorageFileNameByMd5(fileMd5);
                        if (StringUtils.containsWhitespace(existingStorageFileName)) {
                            log.info("文件内容已存在，复用文件：originalFilename={}, storageFileName={}",
                                    originalFilename, existingStorageFileName);
                            String fileUrl = getFileUrl(existingStorageFileName, DEFAULT_URL_EXPIRY_SECONDS);
                            return buildResultMap(originalFilename, existingStorageFileName, fileUrl, fileMd5);
                        }

                        // 上传文件（带原始文件名元数据）
                        String realContentType = FileMagicNumberUtil.getRealContentType(uploadInputStream, originalFilename);
                        // 如果真实类型未匹配到，用客户端传递的类型（或默认类型）
                        if (StringUtils.isEmpty(realContentType)) {
                            realContentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
                        }
                        minioClient.putObject(
                                PutObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(storageFileName)
                                        .stream(uploadInputStream, file.getSize(), -1)
                                        .contentType(realContentType)
                                        .userMetadata(Collections.singletonMap(ORIGINAL_FILENAME_META_KEY, originalFilename))
                                        .build()
                        );

                        // 存储MD5-文件名映射
                        saveMd5StorageNameMapping(fileMd5, storageFileName);

                        log.info("文件上传成功：originalFilename={}, storageFileName={}, md5={}, size={}KB",
                                originalFilename, storageFileName, fileMd5, file.getSize() / 1024);

                        // 返回结果
                        String fileUrl = getFileUrl(storageFileName, DEFAULT_URL_EXPIRY_SECONDS);
                        return buildResultMap(originalFilename, storageFileName, fileUrl, fileMd5);
                    } finally {
                        if (lockAcquired) {
                            distributedLockService.releaseLock(lockKey, requestId);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("文件异步上传失败：originalFilename={}", originalFilename, e);
                throw new RuntimeException("文件上传失败：" + e.getMessage());
            }
        }, uploadTaskExecutor);
    }

    /**
     * 构建返回结果Map
     */
    private Map<String, String> buildResultMap(String originalFilename, String storageFileName, String fileUrl, String fileMd5) {
        Map<String, String> result = new HashMap<>();
        result.put("originalFilename", originalFilename); // 原始文件名
        result.put("storageFileName", storageFileName);   // 存储文件名（MinIO的object名）
        result.put("fileUrl", fileUrl);                   // 预签名URL
        result.put("fileMd5", fileMd5);                   // 文件MD5
        return result;
    }

    /**
     * 存储MD5与存储文件名的映射（Redis，有效期7天）
     */
    private void saveMd5StorageNameMapping(String fileMd5, String storageFileName) {
        String redisKey = "file:md5:mapping:" + fileMd5;
        stringRedisTemplate.opsForValue().set(redisKey, storageFileName, 7, TimeUnit.DAYS);
    }

    /**
     * 根据MD5查询已存在的存储文件名
     */
    private String getStorageFileNameByMd5(String fileMd5) {
        String redisKey = "file:md5:mapping:" + fileMd5;
        return stringRedisTemplate.opsForValue().get(redisKey);
    }
    /**
     * 单文件异步下载（流式返回，避免内存溢出）
     * @param storageFileName 文件名（MD5+后缀）
     * @return 文件输入流（自动关闭）
     */
    @Async("downloadTaskExecutor")
    public CompletableFuture<Map<String, Object>> downloadFileAsync(String storageFileName) {
        return CompletableFuture.supplyAsync(() -> {
            Assert.hasText(storageFileName, "存储文件名不能为空");

            try {
                // 检查文件是否存在
                if (!checkFileExists(storageFileName)) {
                    throw new RuntimeException("文件不存在：" + storageFileName);
                }

                // 获取文件元数据（原始文件名、大小、Content-Type）
                StatObjectResponse statResponse = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageFileName)
                                .build()
                );
                String originalFilename = statResponse.userMetadata().getOrDefault(
                        ORIGINAL_FILENAME_META_KEY,
                        storageFileName.split("_")[1] // 兼容无元数据
                );
                String contentType = statResponse.contentType(); // MinIO 存储的真实 Content-Type
                long contentLength = statResponse.size(); // 文件大小（字节）

                // 获取文件流（MinIO 的 GetObjectResponse）
                GetObjectResponse response = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageFileName)
                                .build()
                );

                // 包装结果（含文件大小）
                Map<String, Object> result = new HashMap<>();
                result.put("originalFilename", originalFilename);
                result.put("inputStream", response); // 仍是 GetObjectResponse，后续 Controller 包装
                result.put("contentType", contentType);
                result.put("contentLength", contentLength); // 新增文件大小

                log.info("文件下载流获取成功：storageFileName={}, originalFilename={}, size={}KB",
                        storageFileName, originalFilename, contentLength / 1024);
                return result;
            } catch (Exception e) {
                log.error("文件异步下载失败：storageFileName={}", storageFileName, e);
                throw new RuntimeException("文件下载失败：" + e.getMessage());
            }
        }, downloadTaskExecutor);
    }

    /**
     * 文件删除（支持批量删除优化）
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) {
        Assert.hasText(fileName, "文件名不能为空");

        try {
            if (!checkFileExists(fileName)) {
                log.warn("文件不存在，跳过删除：{}", fileName);
                return;
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("文件删除成功：{}", fileName);
        } catch (Exception e) {
            log.error("文件删除失败：{}", fileName, e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取预签名访问URL（支持自定义有效期）
     * @param fileName 文件名
     * @param expirySeconds 有效期（秒）
     * @return 预签名URL
     */
    public String getFileUrl(String fileName, int expirySeconds) {
        Assert.hasText(fileName, "文件名不能为空");
        Assert.isTrue(expirySeconds > 0 && expirySeconds <= 24 * 3600, "有效期需在1-86400秒之间");

        try {
            if (!checkFileExists(fileName)) {
                throw new RuntimeException("文件不存在：" + fileName);
            }

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(expirySeconds)
                            .build()
            );
            log.info("生成文件预签名URL：fileName={}, expiry={}s", fileName, expirySeconds);
            return url;
        } catch (Exception e) {
            log.error("生成文件URL失败：{}", fileName, e);
            throw new RuntimeException("获取文件访问地址失败：" + e.getMessage());
        }
    }

    /**
     * 重载：使用默认有效期获取URL
     */
    public String getFileUrl(String fileName) {
        return getFileUrl(fileName, DEFAULT_URL_EXPIRY_SECONDS);
    }

    // ==================== 分片上传逻辑（Redis 跟踪分片）====================

    /**
     * 分片上传元数据（存储在Redis，跟踪分片状态）
     */
    @Data
    private static class MultipartMeta {
        private String originalFilename; // 原始文件名
        private String storageFileName;  // 存储文件名（UUID_原始名）
        private String fileMd5;
        private int totalParts;
        private Set<Integer> uploadedParts;
        private long createTime;
    }

    /**
     * 初始化分片上传（生成uploadId，Redis存储元数据）
     * @param fileMd5 文件MD5（唯一标识）
     * @param originalFilename 原始文件名（用于获取后缀）
     * @param totalParts 总分片数
     * @return uploadId（用于后续分片关联）
     */
    public String initMultipartUpload(String fileMd5, String originalFilename, int totalParts) {
        Assert.hasText(fileMd5, "文件MD5不能为空");
        Assert.hasText(originalFilename, "原始文件名不能为空");
        Assert.isTrue(totalParts > 0, "总分片数必须大于0");

        try {
            // 处理原始文件名，生成存储文件名
            String safeOriginalName = processSafeFilename(originalFilename);
            String storageFileName = UUID.randomUUID().toString().replace("-", "") + "_" + safeOriginalName;

            // 生成uploadId
            String uploadId = UUID.randomUUID().toString().replace("-", "");

            // 构建分片元数据（包含原始文件名）
            MultipartMeta meta = new MultipartMeta();
            meta.setOriginalFilename(originalFilename);
            meta.setStorageFileName(storageFileName);
            meta.setFileMd5(fileMd5);
            meta.setTotalParts(totalParts);
            meta.setUploadedParts(new HashSet<>());
            meta.setCreateTime(System.currentTimeMillis());

            // 存储到Redis
            String redisKey = MULTIPART_META_PREFIX + uploadId;
            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    JSON.toJSONString(meta),
                    MULTIPART_META_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );

            log.info("分片上传初始化成功：uploadId={}, originalFilename={}, storageFileName={}, totalParts={}",
                    uploadId, originalFilename, storageFileName, totalParts);
            return uploadId;
        } catch (Exception e) {
            log.error("分片上传初始化失败：fileMd5={}, originalFilename={}", fileMd5, originalFilename, e);
            throw new RuntimeException("分片上传初始化失败：" + e.getMessage());
        }
    }

    /**
     * 上传分片
     * @param uploadId 分片上传ID（init返回）
     * @param partNumber 分片编号（从1开始，连续不重复）
     * @param chunkFile 分片文件
     */
    public String uploadChunk(String uploadId, int partNumber, MultipartFile chunkFile) {
        Assert.hasText(uploadId, "分片上传ID不能为空");
        Assert.isTrue(partNumber > 0, "分片编号必须大于0");
        Assert.notNull(chunkFile, "分片文件不能为空");
        Assert.isTrue(chunkFile.getSize() > 0, "分片文件大小不能为0");

        // 从Redis获取分片元数据
        String redisKey = MULTIPART_META_PREFIX + uploadId;
        String metaJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (metaJson == null) {
            throw new RuntimeException("分片上传元数据不存在，可能已过期：uploadId=" + uploadId);
        }
        MultipartMeta meta = JSON.parseObject(metaJson, new TypeReference<>() {});

        // 分布式锁：防止同一分片并发上传
        String lockKey = CHUNK_LOCK_PREFIX + meta.getFileMd5() + ":" + partNumber;
        String requestId = UUID.randomUUID().toString();
        boolean lockAcquired = distributedLockService.tryLock(lockKey, requestId, 15, 2, 100);

        try (InputStream chunkInputStream = chunkFile.getInputStream()) {
            if (!lockAcquired) {
                throw new RuntimeException("分片" + partNumber + "正在上传中，请勿重复提交");
            }

            // 校验分片编号是否合法（不超过总分片数）
            if (partNumber > meta.getTotalParts()) {
                throw new RuntimeException("分片编号非法：当前" + partNumber + "，最大" + meta.getTotalParts());
            }

            // 校验分片是否已上传（避免重复上传）
            if (meta.getUploadedParts().contains(partNumber)) {
                log.info("分片已上传，跳过：uploadId={}, partNumber={}", uploadId, partNumber);
                return meta.getOriginalFilename();
            }

            // 分片对象名格式：fileName-part-partNumber（便于后续合并）
            String chunkObjectName = meta.getOriginalFilename() + "-part-" + partNumber;

            // 上传分片到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(chunkObjectName)
                            .stream(chunkInputStream, chunkFile.getSize(), CHUNK_SIZE)
                            .contentType("application/octet-stream")
                            .build()
            );

            // 更新Redis元数据（添加已上传分片号，续期）
            meta.getUploadedParts().add(partNumber);
            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    JSON.toJSONString(meta),
                    MULTIPART_META_EXPIRE_SECONDS,
                    java.util.concurrent.TimeUnit.SECONDS
            );

            log.info("分片上传成功：uploadId={}, fileName={}, partNumber={}, chunkSize={}KB",
                    uploadId, meta.getOriginalFilename(), partNumber, chunkFile.getSize() / 1024);
        } catch (Exception e) {
            log.error("分片上传失败：uploadId={}, partNumber={}", uploadId, partNumber, e);
            throw new RuntimeException("分片" + partNumber + "上传失败：" + e.getMessage());
        } finally {
            if (lockAcquired) {
                distributedLockService.releaseLock(lockKey, requestId);
            }
        }
        return meta.getOriginalFilename();
    }

    /**
     * 合并分片
     * @param uploadId 分片上传ID
     * @return 预签名访问URL
     */
    public Map<String, String>  completeMultipartUpload(String uploadId) {
        Assert.hasText(uploadId, "分片上传ID不能为空");

        String redisKey = MULTIPART_META_PREFIX + uploadId;
        try {
            // 获取并校验分片元数据
            String metaJson = stringRedisTemplate.opsForValue().get(redisKey);
            if (metaJson == null) {
                throw new RuntimeException("分片上传元数据不存在，可能已过期：uploadId=" + uploadId);
            }
            MultipartMeta meta = JSON.parseObject(metaJson, new TypeReference<MultipartMeta>() {});
            String storageFileName = meta.getStorageFileName();
            String originalFilename = meta.getOriginalFilename();
            String fileMd5 = meta.getFileMd5();

            // 校验分片完整性（已上传分片数 == 总分片数）
            if (meta.getUploadedParts().size() != meta.getTotalParts()) {
                throw new RuntimeException("分片数量不完整：已上传" + meta.getUploadedParts().size() + "个，需" + meta.getTotalParts() + "个");
            }

            // 校验分片编号连续性（避免缺失中间分片）
            for (int i = 1; i <= meta.getTotalParts(); i++) {
                if (!meta.getUploadedParts().contains(i)) {
                    throw new RuntimeException("分片缺失：partNumber=" + i);
                }
            }

            // 构建合并源（按分片编号顺序排列）
            List<ComposeSource> sources = new ArrayList<>(meta.getTotalParts());
            for (int i = 1; i <= meta.getTotalParts(); i++) {
                String chunkObjectName = originalFilename + "-part-" + i;
                // 校验分片文件是否存在（防止被误删）
                if (!checkFileExists(chunkObjectName)) {
                    throw new RuntimeException("分片文件不存在：" + chunkObjectName);
                }
                sources.add(ComposeSource.builder()
                        .bucket(bucketName)
                        .object(chunkObjectName)
                        .build());
            }

            // 合并分片为最终文件
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageFileName)
                            .sources(sources)
                            // 存储原始文件名到元数据
                            .userMetadata(Collections.singletonMap(ORIGINAL_FILENAME_META_KEY, originalFilename))
                            .build()
            );

            log.info("分片合并成功：uploadId={}, fileName={}, totalParts={}",
                    uploadId, originalFilename, meta.getTotalParts());

            // 异步删除分片文件（非核心流程，不阻塞合并结果返回）
            CompletableFuture.runAsync(() -> {
                for (int i = 1; i <= meta.getTotalParts(); i++) {
                    String chunkObjectName = originalFilename + "-part-" + i;
                    try {
                        deleteFile(chunkObjectName);
                    } catch (Exception e) {
                        log.error("分片文件删除失败：{}", chunkObjectName, e);
                        // 可记录到异常表，后续人工清理
                    }
                }
                // 删除Redis元数据
                stringRedisTemplate.delete(redisKey);
                log.info("分片文件及元数据清理完成：uploadId={}", uploadId);
            }, uploadTaskExecutor);

            // 返回最终文件的预签名URL
            String fileUrl = getFileUrl(storageFileName, DEFAULT_URL_EXPIRY_SECONDS);
            return buildResultMap(originalFilename, storageFileName, fileUrl, fileMd5);
        } catch (Exception e) {
            log.error("分片合并失败：uploadId={}", uploadId, e);
            throw new RuntimeException("分片合并失败：" + e.getMessage());
        }
    }

    /**
     * 取消分片上传（清理分片文件和Redis元数据）
     * @param uploadId 分片上传ID
     */
    public void abortMultipartUpload(String uploadId) {
        Assert.hasText(uploadId, "分片上传ID不能为空");

        String redisKey = MULTIPART_META_PREFIX + uploadId;
        try {
            // 获取分片元数据
            String metaJson = stringRedisTemplate.opsForValue().get(redisKey);
            if (metaJson == null) {
                log.warn("分片上传元数据不存在，无需取消：uploadId={}", uploadId);
                return;
            }
            MultipartMeta meta = JSON.parseObject(metaJson, new TypeReference<MultipartMeta>() {});
            String fileName = meta.getOriginalFilename();

            // 删除已上传的分片文件
            for (int partNumber : meta.getUploadedParts()) {
                String chunkObjectName = fileName + "-part-" + partNumber;
                try {
                    deleteFile(chunkObjectName);
                    log.info("取消分片上传：删除分片文件{}", chunkObjectName);
                } catch (Exception e) {
                    log.error("取消分片上传：删除分片文件失败{}", chunkObjectName, e);
                }
            }

            // 删除Redis元数据
            stringRedisTemplate.delete(redisKey);
            log.info("取消分片上传成功：uploadId={}, 已清理{}个分片文件",
                    uploadId, meta.getUploadedParts().size());
        } catch (Exception e) {
            log.error("取消分片上传失败：uploadId={}", uploadId, e);
            throw new RuntimeException("取消分片上传失败：" + e.getMessage());
        }
    }

    /**
     * 查询分片上传状态（用于断点续传）
     * @param uploadId 分片上传ID
     * @return 已上传分片号列表
     */
    public List<Integer> queryMultipartUploadStatus(String uploadId) {
        Assert.hasText(uploadId, "分片上传ID不能为空");

        String redisKey = MULTIPART_META_PREFIX + uploadId;
        String metaJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (metaJson == null) {
            throw new RuntimeException("分片上传元数据不存在，可能已过期：uploadId=" + uploadId);
        }

        MultipartMeta meta = JSON.parseObject(metaJson, new TypeReference<MultipartMeta>() {});
        // 返回排序后的分片号列表
        return meta.getUploadedParts().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    // ==================== 同步上传到多目标（优化阻塞调用）====================

    /**
     * 异步同步上传文件到多个目标URL（非阻塞+并发安全）
     * @param file 上传文件
     * @param targetUrls 目标URL数组
     * @param taskId 任务ID
     * @return 异步结果
     */
    @Async("uploadTaskExecutor")
    public CompletableFuture<Void> syncUploadFileAsync(MultipartFile file, String[] targetUrls, String taskId) {
        Assert.notNull(file, "上传文件不能为空");
        Assert.notNull(targetUrls, "目标URL数组不能为空");
        Assert.isTrue(targetUrls.length > 0, "目标URL数组不能为空");
        Assert.hasText(taskId, "任务ID不能为空");

        return CompletableFuture.runAsync(() -> {
            try {
                // 先上传到本地MinIO（用join()替代get()，避免受检异常）
                String fileUrl = uploadFileAsync(file).join().get("fileUrl");
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

                // 更新任务状态（本地上传完成：30%进度）
                updateTaskStatus(taskId, "uploaded_to_local", 30, null, fileName, null);

                // 计算每个目标的进度增量（70%分配给多目标上传）
                int progressIncrement = 70 / targetUrls.length;

                // 非阻塞并行上传到所有目标
                Flux.fromArray(targetUrls)
                        .parallel() // 并行执行
                        .runOn(reactor.core.scheduler.Schedulers.fromExecutor(uploadTaskExecutor))
                        .flatMap(targetUrl -> uploadToSingleTarget(file, targetUrl, taskId, progressIncrement))
                        .sequential()
                        .blockLast(); // 等待所有上传完成（非阻塞当前线程池）

                // 更新最终状态（100%）
                updateTaskStatus(taskId, "completed", 100, fileUrl, fileName, null);
            } catch (Exception e) {
                log.error("同步上传任务失败：taskId={}", taskId, e);
                String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                updateTaskStatus(taskId, "failed", 0, null, null, errorMsg);
                throw new RuntimeException("同步上传任务失败：" + errorMsg);
            }
        }, uploadTaskExecutor);
    }

    /**
     * 上传文件到单个目标URL（multipart/form-data格式）
     * @param file 上传文件
     * @param targetUrl 目标URL
     * @param taskId 任务ID
     * @param progressIncrement 进度增量
     * @return 上传结果Mono
     */
    private Mono<String> uploadToSingleTarget(MultipartFile file, String targetUrl, String taskId, int progressIncrement) {
        return webClient.post()
                .uri(targetUrl)
                .contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
                // 用multipart/form-data格式上传，兼容大多数服务端
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(result -> {
                    log.info("同步上传到目标成功：targetUrl={}, taskId={}", targetUrl, taskId);
                    // 并发安全更新进度
                    updateTaskProgress(taskId, progressIncrement);
                })
                .doOnError(error -> {
                    String errorMsg = "同步上传到目标失败：targetUrl=" + targetUrl + ", error=" + error.getMessage();
                    log.error(errorMsg, error);
                    // 失败也更新进度（避免卡进度）
                    updateTaskProgress(taskId, progressIncrement);
                });
    }

    // ==================== 辅助方法（并发安全+封装）====================

    /**
     * 检查文件是否存在
     */
    private boolean checkFileExists(String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            // 捕获文件不存在异常，返回false
            if (e.getMessage() != null && e.getMessage().contains("Object does not exist")) {
                return false;
            }
            // 其他异常抛出
            throw new RuntimeException("检查文件是否存在失败：" + e.getMessage(), e);
        }
    }

    /**
     * 并发安全更新任务进度
     */
    private void updateTaskProgress(String taskId, int increment) {
        // 用分布式锁保证进度更新原子性
        String lockKey = "task:progress:" + taskId;
        String requestId = UUID.randomUUID().toString();
        boolean lockAcquired = distributedLockService.tryLock(lockKey, requestId, 5, 2, 50);

        try {
            if (lockAcquired) {
                Map<String, Object> taskInfo = syncUploadTaskService.getTask(taskId);
                if (taskInfo == null) {
                    log.warn("任务不存在，跳过进度更新：taskId={}", taskId);
                    return;
                }

                Integer currentProgress = (Integer) taskInfo.getOrDefault("progress", 30);
                int newProgress = Math.min(currentProgress + increment, 90); // 最大90%，留10%给最终状态
                syncUploadTaskService.updateTask(taskId, Collections.singletonMap("progress", newProgress));
            }
        } finally {
            if (lockAcquired) {
                distributedLockService.releaseLock(lockKey, requestId);
            }
        }
    }

    /**
     * 更新任务状态（统一封装）
     */
    private void updateTaskStatus(String taskId, String status, int progress, String fileUrl, String fileName, String error) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("progress", progress);
        if (fileUrl != null) updates.put("fileUrl", fileUrl);
        if (fileName != null) updates.put("fileName", fileName);
        if (error != null) updates.put("error", error);
        if ("completed".equals(status) || "failed".equals(status)) {
            updates.put(status.equals("completed") ? "completedAt" : "failedAt", System.currentTimeMillis());
        }
        syncUploadTaskService.updateTask(taskId, updates);
    }

    /**
     * 处理文件名特殊字符（替换 MinIO 不支持的字符）
     * @param originalFilename 原始文件名
     * @return 处理后的安全文件名
     */
    private String processSafeFilename(String originalFilename) {
        if (StringUtils.isEmpty(originalFilename)) {
            return UUID.randomUUID().toString() + ".tmp";
        }
        // 替换特殊字符为下划线
        String safeName = SPECIAL_CHAR_PATTERN.matcher(originalFilename).replaceAll("_");
        // 限制文件名长度（避免超出 MinIO 限制，默认不超过255字符）
        if (safeName.length() > 255) {
            String extension = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf(".")) : ".tmp";
            String namePrefix = safeName.substring(0, 255 - extension.length() - 1);
            safeName = namePrefix + "_" + extension;
        }
        return safeName;
    }
}