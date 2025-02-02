package com.example.demo.global.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Client Error (400번대)
    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력값입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C003", "잘못된 타입입니다"),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C004", "인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "C005", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "C006", "만료된 토큰입니다"),

    // 403
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C007", "접근이 거부되었습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C008", "권한이 없습니다"),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "C009", "리소스를 찾을 수 없습니다"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C010", "엔티티를 찾을 수 없습니다"),

    // 405
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C011", "허용되지 않은 메서드입니다"),

    // 409
    CONFLICT(HttpStatus.CONFLICT, "C012", "리소스 충돌이 발생했습니다"),

    // Server Error (500번대)
    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 오류가 발생했습니다"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "데이터베이스 오류가 발생했습니다"),

    // 503
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "S003", "서비스를 사용할 수 없습니다");

    private final HttpStatus status; // HTTP 상태 코드
    private final String code; // 내부 에러 코드
    private final String message; // 에러 메시지
}
