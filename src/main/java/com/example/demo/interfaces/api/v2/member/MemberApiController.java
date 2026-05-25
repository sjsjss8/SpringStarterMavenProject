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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 회원 REST API (v2 — SPA 식 JSON 통신).
 *
 * <p>이 컨트롤러는 v2 패키지 = "SPA(Vue 3) 로 UI 를 해결하는 방식" 에 속한다.
 * v1 (Thymeleaf) 의 JS 도 호환을 위해 이 v2 API 를 호출한다 — 즉 모든 클라이언트는
 * 단일 REST API 를 공유한다.</p>
 *
 * <p>경로: {@code /api/v2/members/**}</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/members")
public class MemberApiController {

    @Qualifier("MemberService")
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(memberService.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> search(
            @ModelAttribute @Valid MemberRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.search(requestDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponseDto>> insert(
            @RequestBody @Valid MemberRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(memberService.insert(requestDto)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<MemberResponseDto>> update(
            @PathVariable String id,
            @RequestBody @Valid MemberRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.update(id, requestDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> delete(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.delete(id)));
    }
}
