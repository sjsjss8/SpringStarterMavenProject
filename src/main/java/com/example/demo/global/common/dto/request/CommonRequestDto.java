package com.example.demo.global.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "공통 요청 파라미터 — 페이징/정렬/검색/기간 필터를 모든 요청 DTO 가 상속받아 사용")
public class CommonRequestDto {

    // 페이징 관련
    @Schema(description = "현재 페이지 (0부터 시작)", example = "0", defaultValue = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
    @Builder.Default
    private Integer size = 10;

    // 정렬 관련
    @Schema(description = "정렬 기준 필드", example = "id")
    private String sortBy;

    @Schema(description = "정렬 방향 (ASC | DESC)", example = "DESC", defaultValue = "DESC")
    @Builder.Default
    private String sortDirection = "DESC";

    // 검색 관련
    @Schema(description = "검색 유형 (예: id, name, email)", example = "name")
    private String searchType;

    @Schema(description = "검색 키워드 (부분 일치)", example = "홍길")
    private String searchKeyword;

    // 기간 검색
    @Schema(description = "조회 시작일 (yyyy-MM-dd)", example = "2026-01-01")
    private String startDate;

    @Schema(description = "조회 종료일 (yyyy-MM-dd)", example = "2026-12-31")
    private String endDate;

    // 공통 필터
    @Schema(description = "상태값", example = "ACTIVE")
    private String status;

    @Schema(description = "카테고리", example = "GENERAL")
    private String category;

    @Schema(description = "사용 여부 (true/false)", example = "true")
    private Boolean useYn;
}
