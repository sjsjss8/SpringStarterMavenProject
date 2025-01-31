package com.example.demo.global.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseResponse<T> {
    private T data;
    private Meta meta;
    
    @Getter
    @AllArgsConstructor
    public static class Meta {
        private int code;
        private String message;
        private String errorCode;  // 에러 코드 추가
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data, new Meta(200, "Success", null));
    }
    
    public static <T> BaseResponse<T> error(int code, String message, String errorCode) {
        return new BaseResponse<>(null, new Meta(code, message, errorCode));
    }
}
