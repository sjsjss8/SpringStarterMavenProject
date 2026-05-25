package com.example.demo.global.common.api;

import java.util.List;

import com.example.demo.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "모든 REST API 의 공통 응답 래퍼. 성공 시 `data`, 실패 시 `error` 가 채워진다.")
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    @Schema(description = "응답 데이터 (성공 시 채워짐, 실패 시 생략)")
    private T data;

    @Schema(description = "에러 정보 (실패 시 채워짐, 성공 시 생략)")
    private Error error;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "에러 상세")
    public static class Error {
        @Schema(description = "에러 코드", example = "M001")
        private String code;

        @Schema(description = "에러 메시지", example = "존재하지 않는 회원입니다.")
        private String message;

        @Schema(description = "필드별 검증 에러 목록 (요청 DTO 검증 실패 시)")
        private List<FieldError> errors;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "필드별 검증 에러")
    public static class FieldError {
        @Schema(description = "에러 발생 필드명", example = "email")
        private String field;

        @Schema(description = "요청에 담겨 온 잘못된 값", example = "not-an-email")
        private String value;

        @Schema(description = "에러 이유", example = "올바른 이메일 형식이 아닙니다.")
        private String reason;
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
