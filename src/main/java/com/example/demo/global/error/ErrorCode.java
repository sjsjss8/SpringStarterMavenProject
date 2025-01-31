package com.example.demo.global.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Common Error (1000번대)
    INVALID_INPUT_VALUE(1000, HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(1001, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    
    // Member Error (2000번대)
    MEMBER_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(2001, HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    
    // Board Error (3000번대)
    BOARD_NOT_FOUND(3000, HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
    
    private final int code;          // 에러 코드
    private final HttpStatus status; // HTTP 상태
    private final String message;    // 에러 메시지
}

