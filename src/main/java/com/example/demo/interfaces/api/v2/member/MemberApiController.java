package com.example.demo.interfaces.api.v2.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.application.member.dto.response.MemberResponseDto;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.global.common.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 회원 REST API (v2 — SPA 식 JSON 통신).
 *
 * <p>이 컨트롤러는 v2 패키지 = "SPA(Vue 3) 로 UI 를 해결하는 방식" 에 속한다.
 * Vue SPA 의 {@code src/main/frontend/src/api/http.ts} 가 이 엔드포인트를 호출한다.</p>
 *
 * <p>v1 ({@link com.example.demo.interfaces.api.v1.member.MemberApiController}) 와
 * 시그니처는 동일하지만, 두 API 는 의도적으로 분리되어 있다 — v1 은 Thymeleaf 페이지 호환용으로
 * 안정성을 우선하고, v2 는 SPA 전용으로 새 필드/응답 포맷을 자유롭게 도입할 수 있다.</p>
 *
 * <p>경로: {@code /api/v2/members/**}</p>
 */
@RestController("memberApiControllerV2")    // ← 명시적 빈 이름 (v1 과 클래스명이 같으므로 충돌 방지)
@RequiredArgsConstructor
@RequestMapping("/api/v2/members")
@Tag(
        name = "Member v2 (SPA 전용)",
        description = "Vue 3 SPA 가 호출하는 회원 REST API. 신규 필드/응답 포맷을 자유롭게 도입 가능."
)
public class MemberApiController {

    @Qualifier("MemberService")
    private final MemberService memberService;

    @Operation(
            summary = "회원 전체 조회 (SPA)",
            description = "SPA 의 목록 화면 초기 로딩에서 사용. 대규모 데이터는 `/search` 페이징 권장."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(memberService.findAll()));
    }

    @Operation(
            summary = "회원 동적 검색 (SPA)",
            description = "SPA 의 검색 폼이 호출. `searchType` 에 따라 id/name/email 컬럼에 LIKE 검색."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> search(
            @ModelAttribute @Valid MemberRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.search(requestDto)));
    }

    @Operation(summary = "회원 단건 조회 (SPA)", description = "SPA 상세 화면 진입 시 호출.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 회원 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findById(
            @Parameter(description = "회원 ID", example = "user01", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.findById(id)));
    }

    @Operation(summary = "회원 등록 (SPA)", description = "SPA 등록 폼 submit 시 호출.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "ID 중복")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponseDto>> insert(
            @RequestBody @Valid MemberRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(memberService.insert(requestDto)));
    }

    @Operation(summary = "회원 수정 (SPA)", description = "PUT / PATCH 모두 허용. 전체 필드 교체.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 회원 없음")
    })
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<MemberResponseDto>> update(
            @Parameter(description = "수정 대상 회원 ID", example = "user01", required = true)
            @PathVariable String id,
            @RequestBody @Valid MemberRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.update(id, requestDto)));
    }

    @Operation(summary = "회원 삭제 (SPA)", description = "응답에 삭제된 회원 정보 포함.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 회원 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> delete(
            @Parameter(description = "삭제 대상 회원 ID", example = "user01", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.delete(id)));
    }
}
