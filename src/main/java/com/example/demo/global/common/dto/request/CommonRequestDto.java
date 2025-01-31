package com.example.demo.global.common.dto.request;

import lombok.Builder;
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
public class CommonRequestDto {
    // 페이징 관련
    @Builder.Default
    private Integer page = 0;          // 현재 페이지 (0부터 시작)
    @Builder.Default
    private Integer size = 10;         // 페이지 크기
    
    // 정렬 관련
    private String sortBy;             // 정렬 기준 필드
    @Builder.Default
    private String sortDirection = "DESC";  // 정렬 방향
    
    // 검색 관련
    private String searchType;         // 검색 유형
    private String searchKeyword;      // 검색어
    
    // 기간 검색
    private String startDate;   // 시작일
    private String endDate;     // 종료일
    
    // 공통 필터
    private String status;             // 상태값
    private String category;           // 카테고리
    private Boolean useYn;             // 사용 여부
}
