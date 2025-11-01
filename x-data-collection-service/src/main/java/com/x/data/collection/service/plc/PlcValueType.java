package com.x.data.collection.service.plc;


import com.x.common.enums.BaseEnum;
import lombok.Getter;


@Getter
public enum PlcValueType implements BaseEnum {
    BOOL(1, "Bool", "BOOL", Boolean.class),

    WORD(2, "Word", "INT16", Integer.class),

    REAL(3, "Real", "FLOAT", Double.class),

    INT(4, "Int", "INT16", Integer.class),

    DINT(5, "DInt", "INT32", Integer.class),

    DINT_TIME(6, "DInt(Time)", "INT32", Integer.class),;

    private final Integer code;

    private final String desc;

    private final String readType;

    private final Class<?> clazz;

    PlcValueType(Integer code, String desc, String readType, Class<?> clazz) {
        this.code = code;
        this.desc = desc;
        this.readType = readType;
        this.clazz = clazz;
    }

    @Override
    public Integer getKey() {
        return code;
    }

    @Override
    public String getValue() {
        return desc;
    }

    public static PlcValueType getByCode(int code) {
        for (PlcValueType value : PlcValueType.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static String getDescByCode(int code) {
        for (PlcValueType value : PlcValueType.values()) {
            if (value.code == code) {
                return value.desc;
            }
        }
        return "";
    }
}
