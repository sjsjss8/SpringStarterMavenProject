package com.example.demo.global.common.api;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.example.demo.global.error.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> { // Response Wrapper
    
    private int code;           // HTTP 상태 코드
    private String status;      // 상태 메시지
    private String message;     // 응답 메시지
    private T data;            // 응답 데이터
    private LocalDateTime timestamp;  // 응답 시간
    
    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .status("SUCCESS")
                .message("정상 처리되었습니다.")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // 성공 응답 (데이터 미포함)
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    // 성공 응답 (사용자 정의 메시지)
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .status("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // 실패 응답
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .code(status.value())
                .status("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // 실패 응답 (예외 처리용)
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getStatus().value())
                .status("ERROR")
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}