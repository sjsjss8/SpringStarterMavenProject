package com.example.demo.global.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private int code;        // 업무 코드
    private String status;   // 상태
    private String message;  // 메시지
    private T data;         // 데이터
    
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "SUCCESS", "정상처리되었습니다.", data);
    }
    
    public static <T> CommonResponse<T> error(int code, String message) {
        return new CommonResponse<>(code, "ERROR", message, null);
    }
}
