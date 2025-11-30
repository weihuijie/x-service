package com.x.file.service.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件魔数（Magic Number）工具类，用于判断文件真实MIME类型
 */
public class FileMagicNumberUtil {
    // 唯一魔数映射（key：魔数字节数组（十六进制字符串），value：基础MIME类型）
    // 确保每个Key唯一，同一魔数对应多种类型时，通过扩展名二次区分
    private static final Map<String, String> UNIQUE_MAGIC_NUMBER_MAP = new HashMap<>();

    static {
        // 图片类型（魔数唯一）
        UNIQUE_MAGIC_NUMBER_MAP.put("89504E47", "image/png"); // PNG
        UNIQUE_MAGIC_NUMBER_MAP.put("FFD8FF", "image/jpeg");  // JPEG/JPG
        UNIQUE_MAGIC_NUMBER_MAP.put("47494638", "image/gif");  // GIF
        // 基础格式（同一魔数对应多种类型，需结合扩展名）
        UNIQUE_MAGIC_NUMBER_MAP.put("504B0304", "application/zip"); // ZIP压缩包（docx/xlsx/pptx 基于此）
        UNIQUE_MAGIC_NUMBER_MAP.put("52494646", "application/riff"); // RIFF容器（WebP/WAV/AVI 基于此）
        UNIQUE_MAGIC_NUMBER_MAP.put("D0CF11E0", "application/msword"); // Office 97-2003（doc/xls）
        // 文本类型（魔数唯一）
        UNIQUE_MAGIC_NUMBER_MAP.put("EFBBBF", "text/plain"); // UTF-8 文本
        UNIQUE_MAGIC_NUMBER_MAP.put("FFFE", "text/plain");   // UTF-16 LE 文本
        UNIQUE_MAGIC_NUMBER_MAP.put("FEFF", "text/plain");   // UTF-16 BE 文本
        // 其他唯一魔数类型
        UNIQUE_MAGIC_NUMBER_MAP.put("25504446", "application/pdf"); // PDF
        UNIQUE_MAGIC_NUMBER_MAP.put("494433", "audio/mpeg");        // MP3（修正错误魔数）
        UNIQUE_MAGIC_NUMBER_MAP.put("00000018", "video/mp4");       // MP4
    }

    /**
     * 读取文件前N个字节（魔数），转为十六进制字符串
     * 注意：重置流指针，不影响后续文件上传
     */
    private static String readMagicNumberHex(InputStream inputStream, int readLength) throws IOException {
        if (!inputStream.markSupported()) {
            throw new IOException("输入流不支持mark/reset，无法读取魔数");
        }
        // 标记当前位置，后续重置
        inputStream.mark(readLength);
        byte[] buffer = new byte[readLength];
        int actualRead = inputStream.read(buffer);
        // 重置流指针到读取前位置
        inputStream.reset();

        if (actualRead <= 0) {
            return "";
        }
        // 转为十六进制字符串（大写，忽略未读满的字节）
        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < actualRead; i++) {
            hexBuilder.append(String.format("%02X", buffer[i]));
        }
        return hexBuilder.toString();
    }

    /**
     * 根据文件流+原始文件名，获取真实MIME类型（魔数优先，扩展名兜底）
     */
    public static String getRealContentType(InputStream inputStream, String originalFilename) throws IOException {
        // 读取前8个字节魔数（覆盖绝大多数文件的魔数长度）
        String magicHex = readMagicNumberHex(inputStream, 8);
        if (ObjectUtils.isEmpty(magicHex)) {
            return getFallbackContentType(originalFilename);
        }

        // 匹配魔数，得到基础类型
        String baseContentType = matchMagicNumber(magicHex);

        // 同一魔数对应多种类型时，结合扩展名二次区分
        if ("application/zip".equals(baseContentType)) {
            // ZIP压缩包：根据扩展名区分 docx/xlsx/pptx
            return getOfficeZipContentType(originalFilename);
        } else if ("application/riff".equals(baseContentType)) {
            // RIFF容器：根据扩展名区分 webp/wav/avi
            return getRiffContentType(originalFilename);
        } else if (StringUtils.containsWhitespace(baseContentType)) {
            // 魔数唯一，直接返回基础类型
            return baseContentType;
        }

        // 未匹配到魔数，按扩展名兜底
        return getFallbackContentType(originalFilename);
    }

    /**
     * 匹配魔数（前缀匹配，兼容不同长度的魔数）
     */
    private static String matchMagicNumber(String magicHex) {
        for (Map.Entry<String, String> entry : UNIQUE_MAGIC_NUMBER_MAP.entrySet()) {
            String magicKey = entry.getKey();
            // 前缀匹配：如JPEG魔数3字节（FFD8FF），匹配前3字节即可
            if (magicHex.startsWith(magicKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * ZIP基础格式：区分Office 2007+格式（docx/xlsx/pptx）
     */
    private static String getOfficeZipContentType(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return switch (extension.toLowerCase()) {
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    /**
     * RIFF容器：区分WebP/WAV/AVI格式
     */
    private static String getRiffContentType(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return switch (extension.toLowerCase()) {
            case "webp" -> "image/webp";
            case "wav" -> "audio/wav";
            case "avi" -> "video/avi";
            default -> "application/octet-stream";
        };
    }

    /**
     * 按扩展名兜底（仅作为最后 fallback）
     */
    private static String getFallbackContentType(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        if (ObjectUtils.isEmpty(extension)) {
            return "application/octet-stream";
        }
        return switch (extension.toLowerCase()) {
            case "txt", "log", "md", "csv" -> "text/plain";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pdf" -> "application/pdf";
            case "mp3" -> "audio/mpeg";
            case "mp4" -> "video/mp4";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    /**
     * 获取文件扩展名（不含点）
     */
    private static String getFileExtension(String originalFilename) {
        if (ObjectUtils.isEmpty(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}