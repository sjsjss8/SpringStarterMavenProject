package com.example.demo.global.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Result<T> {
    private boolean success;
    private T data;
    private String message;
    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(false, null, message);
    }
}
