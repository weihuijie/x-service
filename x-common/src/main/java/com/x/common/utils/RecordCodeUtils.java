package com.x.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

// 工单编码
public class RecordCodeUtils {
    public static String getCode(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        sb.append(sdf.format(new Date()));
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
