package com.x.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Java 时间工具类（基于 Java 8+ java.time API，线程安全）
 * 包含：时间戳、日期加减、起止时间、格式化、比较、时区转换等常用功能
 */
public class DateTimeUtils {

    // ========================== 常用日期格式常量 ==========================
    /** 日期格式：yyyy-MM-dd */
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    /** 时间格式：HH:mm:ss */
    public static final String PATTERN_TIME = "HH:mm:ss";
    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    /** 带毫秒的日期时间格式：yyyy-MM-dd HH:mm:ss.SSS */
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    /** 紧凑日期时间格式：yyyyMMddHHmmss */
    public static final String PATTERN_DATETIME_COMPACT = "yyyyMMddHHmmss";

    // 默认时区（系统时区，可根据需求改为指定时区如 ZoneId.of("Asia/Shanghai")）
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    // 默认日期格式化器（线程安全，可直接复用）
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATETIME).withZone(DEFAULT_ZONE);

    // 私有化构造方法，禁止实例化
    private DateTimeUtils() {}

    // ========================== 时间戳相关操作 ==========================

    /**
     * 获取当前时间戳（毫秒级）
     * @return 毫秒级时间戳（long）
     */
    public static long getCurrentTimestampMs() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 获取当前时间戳（秒级）
     * @return 秒级时间戳（long）
     */
    public static long getCurrentTimestampSec() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 将指定日期字符串转为时间戳（毫秒级）
     * @param dateStr 日期字符串（支持 PATTERN_DATE/PATTERN_DATETIME 等格式）
     * @param pattern 日期格式
     * @return 毫秒级时间戳，转换失败返回 null
     */
    public static Long dateStrToTimestampMs(String dateStr, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(DEFAULT_ZONE);
            Instant instant = LocalDateTime.parse(dateStr, formatter).atZone(DEFAULT_ZONE).toInstant();
            return instant.toEpochMilli();
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将时间戳（毫秒级）转为日期字符串
     * @param timestampMs 毫秒级时间戳
     * @param pattern 目标日期格式
     * @return 格式化后的日期字符串，转换失败返回 null
     */
    public static String timestampMsToDateStr(long timestampMs, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(DEFAULT_ZONE);
            return formatter.format(Instant.ofEpochMilli(timestampMs));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========================== 日期加减操作 ==========================

    /**
     * 获取指定日期的前 N 天
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @param days 前 N 天（正数：前N天，负数：后N天）
     * @return 计算后的日期字符串（yyyy-MM-dd），转换失败返回 null
     */
    public static String getBeforeNDays(String dateStr, int days) {
        return plusOrMinusDays(dateStr, -days);
    }

    /**
     * 获取指定日期的后 N 天
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @param days 后 N 天（正数：后N天，负数：前N天）
     * @return 计算后的日期字符串（yyyy-MM-dd），转换失败返回 null
     */
    public static String getAfterNDays(String dateStr, int days) {
        return plusOrMinusDays(dateStr, days);
    }

    /**
     * 日期加减 N 天（内部通用方法）
     */
    private static String plusOrMinusDays(String dateStr, int days) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATE).withZone(DEFAULT_ZONE);
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            LocalDate resultDate = localDate.plusDays(days);
            return resultDate.format(formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 日期时间加减 N 小时
     * @param dateTimeStr 日期时间字符串（格式：yyyy-MM-dd HH:mm:ss）
     * @param hours 加减小时数（正数：加，负数：减）
     * @return 计算后的日期时间字符串，转换失败返回 null
     */
    public static String plusOrMinusHours(String dateTimeStr, int hours) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATETIME).withZone(DEFAULT_ZONE);
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            LocalDateTime resultDateTime = localDateTime.plusHours(hours);
            return resultDateTime.format(formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 日期时间加减 N 分钟
     * @param dateTimeStr 日期时间字符串（格式：yyyy-MM-dd HH:mm:ss）
     * @param minutes 加减分钟数（正数：加，负数：减）
     * @return 计算后的日期时间字符串，转换失败返回 null
     */
    public static String plusOrMinusMinutes(String dateTimeStr, int minutes) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATETIME).withZone(DEFAULT_ZONE);
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            LocalDateTime resultDateTime = localDateTime.plusMinutes(minutes);
            return resultDateTime.format(formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========================== 日期起止时间（当天/前N天/指定日期） ==========================

    /**
     * 获取当前日期的开始时间（00:00:00.000）
     * @return 开始时间字符串（yyyy-MM-dd 00:00:00.000）
     */
    public static String getCurrentDayStartTime() {
        return getDayStartTime(LocalDate.now());
    }

    /**
     * 获取当前日期的结束时间（23:59:59.999）
     * @return 结束时间字符串（yyyy-MM-dd 23:59:59.999）
     */
    public static String getCurrentDayEndTime() {
        return getDayEndTime(LocalDate.now());
    }

    /**
     * 获取前 N 天的开始时间（00:00:00.000）
     * @param days 前 N 天（如：1=前一天，2=前两天）
     * @return 开始时间字符串，参数非法返回 null
     */
    public static String getBeforeNDaysStartTime(int days) {
        if (days < 0) {
            return null;
        }
        LocalDate beforeDate = LocalDate.now().minusDays(days);
        return getDayStartTime(beforeDate);
    }

    /**
     * 获取前 N 天的结束时间（23:59:59.999）
     * @param days 前 N 天（如：1=前一天，2=前两天）
     * @return 结束时间字符串，参数非法返回 null
     */
    public static String getBeforeNDaysEndTime(int days) {
        if (days < 0) {
            return null;
        }
        LocalDate beforeDate = LocalDate.now().minusDays(days);
        return getDayEndTime(beforeDate);
    }

    /**
     * 获取指定日期的开始时间（00:00:00.000）
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @return 开始时间字符串，转换失败返回 null
     */
    public static String getSpecifyDayStartTime(String dateStr) {
        try {
            LocalDate specifyDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(PATTERN_DATE));
            return getDayStartTime(specifyDate);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定日期的结束时间（23:59:59.999）
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @return 结束时间字符串，转换失败返回 null
     */
    public static String getSpecifyDayEndTime(String dateStr) {
        try {
            LocalDate specifyDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(PATTERN_DATE));
            return getDayEndTime(specifyDate);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 内部通用方法：获取指定 LocalDate 的开始时间
     */
    private static String getDayStartTime(LocalDate localDate) {
        LocalDateTime startTime = localDate.atStartOfDay();
        return startTime.format(DateTimeFormatter.ofPattern(PATTERN_DATETIME_MS));
    }

    /**
     * 内部通用方法：获取指定 LocalDate 的结束时间
     */
    private static String getDayEndTime(LocalDate localDate) {
        LocalDateTime endTime = localDate.atTime(23, 59, 59, 999_999_999);
        return endTime.format(DateTimeFormatter.ofPattern(PATTERN_DATETIME_MS));
    }

    // ========================== 日期格式化与解析 ==========================

    /**
     * 格式化 LocalDateTime 为指定格式的字符串
     * @param localDateTime 要格式化的 LocalDateTime
     * @param pattern 目标格式
     * @return 格式化后的字符串
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(DEFAULT_ZONE);
        return localDateTime.format(formatter);
    }

    /**
     * 将日期字符串解析为 LocalDateTime
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return LocalDateTime 对象，解析失败返回 null
     */
    public static LocalDateTime parseToLocalDateTime(String dateStr, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(DEFAULT_ZONE);
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Date 转 LocalDateTime（兼容旧 API）
     * @param date 旧 Date 对象
     * @return LocalDateTime 对象
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    /**
     * LocalDateTime 转 Date（兼容旧 API）
     * @param localDateTime LocalDateTime 对象
     * @return Date 对象
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        Instant instant = localDateTime.atZone(DEFAULT_ZONE).toInstant();
        return Date.from(instant);
    }

    // ========================== 日期比较与计算 ==========================

    /**
     * 计算两个日期字符串的天数差（date1 - date2）
     * @param date1 日期字符串1（格式：yyyy-MM-dd）
     * @param date2 日期字符串2（格式：yyyy-MM-dd）
     * @return 天数差（正数：date1 在 date2 之后；负数：date1 在 date2 之前；0：相等），计算失败返回 null
     */
    public static Long getDaysDifference(String date1, String date2) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATE).withZone(DEFAULT_ZONE);
            LocalDate localDate1 = LocalDate.parse(date1, formatter);
            LocalDate localDate2 = LocalDate.parse(date2, formatter);
            return ChronoUnit.DAYS.between(localDate2, localDate1);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断日期是否在指定区间内（包含起止日期）
     * @param dateStr 要判断的日期字符串（格式：yyyy-MM-dd）
     * @param startDateStr 开始日期（格式：yyyy-MM-dd）
     * @param endDateStr 结束日期（格式：yyyy-MM-dd）
     * @return true：在区间内；false：不在区间内；解析失败返回 false
     */
    public static boolean isDateInRange(String dateStr, String startDateStr, String endDateStr) {
        LocalDate date = parseToLocalDate(dateStr);
        LocalDate startDate = parseToLocalDate(startDateStr);
        LocalDate endDate = parseToLocalDate(endDateStr);
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 解析日期字符串为 LocalDate
     */
    private static LocalDate parseToLocalDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(PATTERN_DATE));
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========================== 其他常用功能 ==========================

    /**
     * 获取当前日期（yyyy-MM-dd）
     * @return 当前日期字符串
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(PATTERN_DATE));
    }

    /**
     * 获取当前时间（HH:mm:ss）
     * @return 当前时间字符串
     */
    public static String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern(PATTERN_TIME));
    }

    /**
     * 获取当前日期时间（yyyy-MM-dd HH:mm:ss）
     * @return 当前日期时间字符串
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 获取指定日期是星期几（中文）
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @return 星期几（如：星期一、星期二），解析失败返回 null
     */
    public static String getWeekOfDate(String dateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(PATTERN_DATE));
            String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
            int weekIndex = localDate.getDayOfWeek().getValue() % 7;
            return weekDays[weekIndex];
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定月份的天数
     * @param year 年份（如：2025）
     * @param month 月份（1-12）
     * @return 该月的天数，参数非法返回 0
     */
    public static int getDaysOfMonth(int year, int month) {
        if (month < 1 || month > 12) {
            return 0;
        }
        LocalDate localDate = LocalDate.of(year, month, 1);
        return localDate.lengthOfMonth();
    }

    /**
     * 获取指定日期所在月份的第一天
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @return 该月第一天（yyyy-MM-dd），解析失败返回 null
     */
    public static String getFirstDayOfMonth(String dateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(PATTERN_DATE));
            LocalDate firstDay = localDate.with(TemporalAdjusters.firstDayOfMonth());
            return firstDay.format(DateTimeFormatter.ofPattern(PATTERN_DATE));
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定日期所在月份的最后一天
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @return 该月最后一天（yyyy-MM-dd），解析失败返回 null
     */
    public static String getLastDayOfMonth(String dateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(PATTERN_DATE));
            LocalDate lastDay = localDate.with(TemporalAdjusters.lastDayOfMonth());
            return lastDay.format(DateTimeFormatter.ofPattern(PATTERN_DATE));
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}