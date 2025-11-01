package com.x.data.collection.service.plc.read;

import com.x.data.collection.service.plc.PlcValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlcReadDto<T> {

    private static Pattern pattern = Pattern.compile("((DB(\\d+)\\.DBX(\\d+)\\.[0-7])|(DB(\\d+)\\.DBB(\\d+)(\\.0)?)|(DB(\\d+)\\.DBW(\\d+)(\\.0)?)|(DB(\\d+)\\.DBD(\\d+)(\\.0)?)|(I(\\d+)\\.[0-7])|(IW(\\d+)(\\.0)?)|(ID(\\d+)(\\.0)?)|(Q(\\d+)\\.[0-7])|(QW(\\d+)(\\.0)?)|(QD(\\d+)(\\.0)?)|(M(\\d+)\\.[0-7])|(MW(\\d+)(\\.0)?)|(MD(\\d+)(\\.0)?))");

    /**
     * 读取类型
     */
    private String valueType;

    /**
     * 完整地址
     */
    private String address;

    /**
     * 去除编号的地址
     */
    private String subAddress;

    /**
     * db编号
     */
    private Integer dbNum;

    /**
     * db块编号
     */
    private String addressPrefix;

    private T value;

    private Class<T> clazz;

    /**
     * 设置读取值
     */
    private Consumer<T> setValue;

    public <J> J getActualValue(Class<J> clazz) {
        if (!clazz.isInstance(value)) {
            return null;
        }
        return clazz.cast(value);
    }

    public void setActualValue(Object value) {
        if (!clazz.isInstance(value)) {
            return;
        }
        this.value = clazz.cast(value);
    }

    public static <T> PlcReadDto<T> build(String address, PlcValueType valueType) {
        var ret = new PlcReadDto<T>();
        if (StringUtils.isBlank(address) || valueType == null) {
            return null;
        }
        if (!isValidPLCAddress(address)) {
            return null;
        }
        ret.address = address;
        if (address.startsWith("DB")) {
            Optional.ofNullable(separatePLCAddress(address)).ifPresent(e -> {
                ret.dbNum = e.getLeft();
                ret.subAddress = e.getRight();
                ret.addressPrefix = "DB" + ret.dbNum;
            });
        } else {
            switch (address.charAt(0)) {
                case 'I' -> ret.addressPrefix = "I";
                case 'Q' -> ret.addressPrefix = "Q";
                case 'M' -> ret.addressPrefix = "M";
            }
            ret.subAddress = address;
        }
        ret.valueType = valueType.getReadType();
        ret.clazz = (Class<T>) valueType.getClazz();
        return ret;
    }

    // 判断是否是合法的PLC地址的方法
    public static boolean isValidPLCAddress(String address) {
        // 更新正则表达式，确保DBX后的a是任意数字，b是0-7的数字；DBB、DBW、DBD后的a是任意数字，b是0或空
        Matcher matcher = pattern.matcher(address);
        return matcher.matches();
    }

    public static boolean isValidPLCAddressAndType(String address, int valueType) {
        // 更新正则表达式，确保DBX后的a是任意数字，b是0-7的数字；DBB、DBW、DBD后的a是任意数字，b是0或空
        Matcher matcher = pattern.matcher(address);
        return matcher.matches() && isValidType(address, valueType);
    }

    public static boolean isValidType(String address, int valueType) {
        if (address.startsWith("IW") || address.startsWith("QW") || address.startsWith("MW") || address.contains("DBW")) {
            return List.of(2,4).contains(valueType);
        }
        if (address.startsWith("ID") || address.startsWith("QD") || address.startsWith("MD") || address.contains("DBD")) {
            return List.of(3,5,6).contains(valueType);
        }
        if (address.startsWith("I") || address.startsWith("Q") || address.startsWith("M") || address.contains("DBX")) {
            return List.of(1).contains(valueType);
        }
        return false;
    }

    // 分离DB块编号和具体地址的方法
    private static Pair<Integer, String> separatePLCAddress(String address) {
        int dotIndex = address.indexOf(".");
        if (dotIndex != -1) {
            var dbBlockNumber = Integer.parseInt(address.substring(2, dotIndex)); // 去掉"DB"部分
            String specificAddress = address.substring(dotIndex + 1);
            return Pair.of(dbBlockNumber, specificAddress);
        }
        return null;
    }

    public static String concatAddress(String dataAddr, String subAddress) {
        if ("I".equals(dataAddr) || "Q".equals(dataAddr) || "M".equals(dataAddr)) {
            return subAddress;
        } else {
            return dataAddr + "." + subAddress;
        }
    }
}
