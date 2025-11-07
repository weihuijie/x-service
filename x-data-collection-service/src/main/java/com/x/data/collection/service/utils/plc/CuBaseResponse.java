package com.x.data.collection.service.utils.plc;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
public class CuBaseResponse<T>{

    private Integer code;

    private String msg;

    private Boolean success;

    private List<PlcSingleRes<T>> data;

    public boolean success() {
        return success != null && success;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlcSingleRes<T> {

        private String reqId;

        private String dataAddr;

        private T data;

        private String msg;

        private Boolean success;

        public boolean success() {
            return success != null && success;
        }
    }
}
