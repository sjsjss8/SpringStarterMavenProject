package com.example.demo.global.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseWrapper<T> {
    private T data;
    private Pagination pagination;
    private Error error;        // 에러 정보 추가
    
    @Getter
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
    
    @Getter
    @AllArgsConstructor
    public static class Error {
        private String code;
        private String message;
    }
    
    // 성공 (페이징)
    public static <T> ResponseWrapper<T> success(T data, Pagination pagination) {
        return new ResponseWrapper<>(data, pagination, null);
    }
    
    // 성공 (페이징 없음)
    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(data, null, null);
    }
    
    // 에러
    public static <T> ResponseWrapper<T> error(String code, String message) {
        return new ResponseWrapper<>(null, null, new Error(code, message));
    }
}
