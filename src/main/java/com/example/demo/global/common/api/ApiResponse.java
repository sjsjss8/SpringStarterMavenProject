package com.example.demo.global.common.api;

import java.util.List;

import com.example.demo.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success; // 성공 여부
    private T data; // 응답 데이터
    private Error error; // 에러 정보 (에러 발생 시)

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Error {
        private String code; // 에러 코드
        private String message; // 상세 에러 메시지
        private List<FieldError> errors; // 필드 에러 목록
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {
        private String field; // 에러 발생 필드
        private String value; // 에러 값
        private String reason; // 에러 이유
    }

    // 성공 응답 (200(OK), 201(Created), 204(No Content))
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    // 실패 응답 (ErrorCode 기반)
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(Error.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .build();
    }

    // 실패 응답 (ErrorCode + 필드 에러)
    public static <T> ApiResponse<T> error(ErrorCode errorCode, List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(Error.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .errors(errors)
                        .build())
                .build();
    }
}