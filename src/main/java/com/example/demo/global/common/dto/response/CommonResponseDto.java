package com.example.demo.global.common.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
//@AllArgsConstructor
@ToString
public class CommonResponseDto {
    // 페이징 관련
    private int page;              // 현재 페이지 번호
    private int size;              // 페이지 크기
    private long totalElements;    // 총 데이터 수
    private int totalPages;        // 총 페이지 수
    
    // 정렬 관련
    private String sortBy;         // 정렬 기준
    private String sortOrder;      // 정렬 방향 (ASC, DESC)
    
    // 검색 관련 (요청 정보 포함)
    private String searchType;     // 검색 유형
    private String searchKeyword;  // 검색어
    
    // 시간 정보
    private LocalDateTime timestamp;    // 응답 시간
    
    // 추가 정보
    private String code;           // 업무 구분 코드
    private String message;        // 메시지
}
