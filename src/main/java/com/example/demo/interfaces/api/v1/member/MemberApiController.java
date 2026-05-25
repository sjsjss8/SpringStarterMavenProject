package com.example.demo.interfaces.api.v1.member;

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
 * 회원 REST API (v1 — Thymeleaf 식 페이지의 JS 가 호출).
 *
 * <p>이 컨트롤러는 v1 패키지에 속한다 — "Thymeleaf 식 UI" 의 한 부분이다.
 * Thymeleaf 페이지가 로드된 후, 페이지 안의 JS (resources/static/js/common/restApi.js) 가
 * 이 엔드포인트를 호출해 데이터를 채워 넣는다.</p>
 *
 * <p>v2 ({@link com.example.demo.interfaces.api.v2.member.MemberApiController}) 와 시그니처는
 * 동일하지만, 두 API 는 의도적으로 분리되어 있다:</p>
 * <ul>
 *   <li>v1: Thymeleaf 페이지 호환용. 안정성 우선. breaking change 금지.</li>
 *   <li>v2: SPA 전용. 새 응답 포맷·필드를 자유롭게 도입 가능.</li>
 * </ul>
 *
 * <p>경로: {@code /api/v1/members/**}</p>
 */
@RestController("memberApiControllerV1")    // ← 명시적 빈 이름 (v2 와 클래스명이 같으므로 충돌 방지)
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(
        name = "Member v1 (Thymeleaf 호환)",
        description = "Thymeleaf 페이지의 JS 가 호출하는 회원 REST API. 안정성 우선 — breaking change 금지."
)
public class MemberApiController {

    @Qualifier("MemberService")
    private final MemberService memberService;

    @Operation(
            summary = "회원 전체 조회",
            description = "DB 의 모든 회원을 페이징 없이 반환한다. 데이터가 많아지면 `/search` 사용 권장."
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
            summary = "회원 동적 검색",
            description = "`searchType` 에 따라 id/name/email 컬럼에 `LIKE %keyword%` 검색. " +
                    "MyBatis 동적 쿼리 사용."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공 (결과 0건이어도 200)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> search(
            @ModelAttribute @Valid MemberRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.search(requestDto)));
    }

    @Operation(
            summary = "회원 단건 조회",
            description = "PK(`id`) 로 회원 한 명을 조회한다."
    )
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

    @Operation(
            summary = "회원 등록",
            description = "신규 회원을 등록한다. `id` 는 클라이언트가 지정 (자동 채번 아님)."
    )
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

    @Operation(
            summary = "회원 수정",
            description = "PUT / PATCH 모두 허용. 현재는 전체 필드 교체 의미로 동작한다."
    )
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

    @Operation(
            summary = "회원 삭제",
            description = "PK(`id`) 기준 삭제. 응답으로 삭제된 회원 정보가 반환된다."
    )
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
