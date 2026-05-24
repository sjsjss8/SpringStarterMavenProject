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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
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
