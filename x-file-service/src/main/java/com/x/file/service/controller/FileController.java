package com.x.file.service.controller;

import com.x.common.base.R;
import com.x.file.service.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 文件操作完整控制器（支持单文件上传、分片上传、断点续传、下载、删除等）
 *
 * @author whj
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // ==================== 单文件上传 ====================
    /**
     * 异步单文件上传（防重复、MD5校验）
     * @param file 上传文件
     * @return 任务提交状态（文件URL后续可通过业务回调或查询获取）
     */
    @PostMapping("/upload")
    public R<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            CompletableFuture<Map<String, String>> uploadFuture = fileStorageService.uploadFileAsync(file);
            Map<String, String> result = uploadFuture.join(); // 实际生产可改为异步回调，此处简化
            return R.data(result,"文件上传成功");
        } catch (Exception e) {
            log.error("单文件上传失败", e);
            return R.fail("文件上传失败：" + e.getMessage());
        }
    }

    // ==================== 分片上传（支持断点续传） ====================
    /**
     * 分片上传-初始化（生成uploadId，用于后续分片关联）
     * @param fileMd5 文件MD5（前端计算，唯一标识）
     * @param originalFilename 原始文件名（用于获取后缀）
     * @param totalParts 总分片数（前端拆分后传入）
     * @return uploadId（分片上传唯一标识）
     */
    @PostMapping("/chunk/init")
    public R<String> initChunkUpload(
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("originalFilename") String originalFilename,
            @RequestParam("totalParts") Integer totalParts
    ) {
        try {
            String uploadId = fileStorageService.initMultipartUpload(fileMd5, originalFilename, totalParts);
            return R.data(uploadId,"分片上传初始化成功");
        } catch (Exception e) {
            log.error("分片上传初始化失败：fileMd5={}", fileMd5, e);
            return R.fail("分片上传初始化失败：" + e.getMessage());
        }
    }

    /**
     * 分片上传-上传单个分片
     * @param uploadId 分片上传标识（init接口返回）
     * @param partNumber 分片编号（从1开始，连续不重复）
     * @param chunkFile 分片文件
     * @return 上传状态
     */
    @PostMapping("/chunk/upload")
    public R<Object> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") Integer partNumber,
            @RequestParam("chunkFile") MultipartFile chunkFile
    ) {
        try {
            fileStorageService.uploadChunk(uploadId, partNumber, chunkFile);
            return R.success("分片" + partNumber + "上传成功");
        } catch (Exception e) {
            log.error("分片上传失败：uploadId={}, partNumber={}", uploadId, partNumber, e);
            return R.fail("分片" + partNumber + "上传失败：" + e.getMessage());
        }
    }

    /**
     * 分片上传-合并所有分片（完成大文件上传）
     * @param uploadId 分片上传标识
     * @return 最终文件预签名URL
     */
    @PostMapping("/chunk/complete")
    public R<Map<String, String>> completeChunkUpload(@RequestParam("uploadId") String uploadId) {
        try {
            Map<String, String> result = fileStorageService.completeMultipartUpload(uploadId);
            return R.data(result,"分片合并成功");
        } catch (Exception e) {
            log.error("分片合并失败：uploadId={}", uploadId, e);
            return R.fail("分片合并失败：" + e.getMessage());
        }
    }

    /**
     * 分片上传-取消上传（清理已上传分片和元数据）
     * @param uploadId 分片上传标识
     * @return 取消状态
     */
    @PostMapping("/chunk/abort")
    public R<Object> abortChunkUpload(@RequestParam("uploadId") String uploadId) {
        try {
            fileStorageService.abortMultipartUpload(uploadId);
            return R.success("分片上传已取消");
        } catch (Exception e) {
            log.error("取消分片上传失败：uploadId={}", uploadId, e);
            return R.fail("取消分片上传失败：" + e.getMessage());
        }
    }

    /**
     * 分片上传-查询状态（断点续传用，返回已上传分片号）
     * @param uploadId 分片上传标识
     * @return 已上传分片号列表（排序后）
     */
    @GetMapping("/chunk/status")
    public R<List<Integer>> queryChunkStatus(@RequestParam("uploadId") String uploadId) {
        try {
            List<Integer> uploadedParts = fileStorageService.queryMultipartUploadStatus(uploadId);

            return R.data(uploadedParts);
        } catch (Exception e) {
            log.error("查询分片状态失败：uploadId={}", uploadId, e);
            return R.fail("查询分片状态失败：" + e.getMessage());
        }
    }

    // ==================== 文件下载 ====================
    /**
     * 异步文件下载（流式返回，支持大文件）
     * @param storageFileName 文件名（MD5+后缀，上传接口返回的fileName）
     * @return 文件流（自动触发浏览器下载）
     */
    @GetMapping("/download/{storageFileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("storageFileName") String storageFileName) {
        try {
            // 1. 异步获取文件数据（流、原始文件名、Content-Type、大小）
            CompletableFuture<Map<String, Object>> downloadFuture = fileStorageService.downloadFileAsync(storageFileName);
            Map<String, Object> result = downloadFuture.join(); // 生产环境建议加超时：join(30, TimeUnit.SECONDS)

            // 2. 解析结果
            String originalFilename = (String) result.get("originalFilename");
            InputStream inputStream = (InputStream) result.get("inputStream"); // 实际是 GetObjectResponse
            String contentType = (String) result.get("contentType");
            Long contentLength = (Long) result.get("contentLength");

            // 3. 关键修复：包装流为 Spring 识别的 InputStreamResource
            InputStreamResource resource = new InputStreamResource(inputStream);

            // 4. 处理响应头（中文文件名编码 + 正确 Content-Type）
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();

            // 设置下载文件名（支持中文）
            headers.add("Content-Disposition", "attachment;filename*=UTF-8''" + encodedFilename);
            // 设置缓存策略
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            // 设置文件大小（可选，提升下载体验）
            if (contentLength != null) {
                headers.add("Content-Length", contentLength.toString());
            }

            // 5. 正确设置 Content-Type（优先用 MinIO 存储的真实类型，兜底二进制流）
            MediaType mediaType;
            if (StringUtils.hasText(contentType)) {
                mediaType = MediaType.parseMediaType(contentType);
            } else {
                mediaType = MediaType.APPLICATION_OCTET_STREAM; // 二进制流（兼容所有文件）
            }
            headers.setContentType(mediaType);

            // 6. 返回 ResponseEntity<InputStreamResource>（Spring 能识别）
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("文件下载失败：storageFileName={}", storageFileName, e);
            // 异常时返回 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ==================== 文件删除 ====================
    /**
     * 删除文件（根据文件名）
     * @param storageFileName 文件名
     * @return 删除状态
     */
    @PostMapping("/delete/{storageFileName}")
    public R<Object> deleteFile(@PathVariable("storageFileName") String storageFileName) {
        try {
            fileStorageService.deleteFile(storageFileName);
            return R.success("文件删除成功");
        } catch (Exception e) {
            log.error("文件删除失败：fileName={}", storageFileName, e);
            return R.fail("文件删除失败：" + e.getMessage());
        }
    }

    // ==================== 预签名URL获取 ====================
    /**
     * 获取文件预签名访问URL（支持自定义有效期）
     * @param fileName 文件名
     * @param expirySeconds 有效期（秒，默认30分钟，最大24小时）
     * @return 预签名URL
     */
    @GetMapping("/url")
    public R<String> getFileResignedUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "expirySeconds", defaultValue = "1800") Integer expirySeconds
    ) {
        try {
            String resignedUrl = fileStorageService.getFileUrl(fileName, expirySeconds);
            return R.data(resignedUrl,"预签名URL获取成功");
        } catch (Exception e) {
            log.error("获取预签名URL失败：fileName={}", fileName, e);
            return R.fail("获取预签名URL失败：" + e.getMessage());
        }
    }

    // ==================== 同步上传到多目标 ====================
    /**
     * 异步同步上传文件到多个目标URL（本地上传+多目标分发）
     * @param file 上传文件
     * @param targetUrls 目标URL数组（多个URL用逗号分隔，如：url1,url2,url3）
     * @param taskId 任务ID（前端生成，用于查询任务状态）
     * @return 任务提交状态
     */
    @PostMapping("/sync/upload")
    public R<Object> syncUploadToTargets(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetUrls") String targetUrls,
            @RequestParam("taskId") String taskId
    ) {
        try {
            // 解析目标URL数组（前端传入格式：url1,url2,url3）
            String[] urlArray = targetUrls.split(",");
            CompletableFuture<Void> syncFuture = fileStorageService.syncUploadFileAsync(file, urlArray, taskId);
            return R.data(taskId,"同步上传任务已提交" );
        } catch (Exception e) {
            log.error("同步上传任务提交失败：taskId={}", taskId, e);
            return R.fail("同步上传任务提交失败：" + e.getMessage());
        }
    }
}