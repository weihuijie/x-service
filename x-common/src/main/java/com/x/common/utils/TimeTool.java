package com.x.common.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class TimeTool {

    public static final DateTimeFormatter monthDateFormat = DateTimeFormat.forPattern("yyyyMM");

    public static final DateTimeFormatter yearDateFormat = DateTimeFormat.forPattern("yyyy");

    public static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static final DateTimeFormatter dateUnsignFormat = DateTimeFormat.forPattern("yyyyMMdd");

    public static final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static final DateTimeFormatter dateTimeExportFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH-mm-ss");

    public static int getCurrentMonth() {
        return Integer.parseInt(monthDateFormat.print(System.currentTimeMillis()));
    }

    public static int getCurrentYear() {
        return Integer.parseInt(yearDateFormat.print(System.currentTimeMillis()));
    }

    public static String getUnsignDateStr(Date date) {
        return dateUnsignFormat.print(date.getTime());
    }
    public static String getDateStr(Date date) {
        return dateFormat.print(date.getTime());
    }

    public static String getDateTimeStr(Date date) {
        return dateTimeFormat.print(date.getTime());
    }

    public static String getExportDateTimeStr(Date date) {
        return dateTimeExportFormat.print(date.getTime());
    }
}
