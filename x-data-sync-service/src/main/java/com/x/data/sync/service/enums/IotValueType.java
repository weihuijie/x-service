package com.x.data.sync.service.enums;


import com.x.common.enums.BaseEnum;
import lombok.Getter;


@Getter
public enum IotValueType implements BaseEnum {
    BOOL(1, "BOOLEAN"),

    WORD(2, "INT32"),

    REAL(3, "FLOAT"),

    INT(4, "INT16"),

    DINT(5, "INT32"),

    DINT_TIME(6, "INT32"),;

    private final Integer code;

    private final String type;

    IotValueType(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    @Override
    public Integer getKey() {
        return code;
    }

    @Override
    public String getValue() {
        return type;
    }

    public static IotValueType getByCode(int code) {
        for (IotValueType value : IotValueType.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static String getDescByCode(int code) {
        for (IotValueType value : IotValueType.values()) {
            if (value.code == code) {
                return value.type;
            }
        }
        return "";
    }
}
