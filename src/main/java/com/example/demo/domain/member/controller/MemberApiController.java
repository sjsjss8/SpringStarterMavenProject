package com.example.demo.domain.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.member.dto.request.MemberRequestDto;
import com.example.demo.domain.member.dto.response.MemberResponseDto;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.global.common.api.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor // lombok이 final 필드에 대한 생성자를 자동 생성..와 진짜 좋아졌다..
public class MemberApiController {
	
	@Qualifier("MemberService")
	private final MemberService memberService;
	
//    @RequiredArgsConstructor를 사용하면 @Autowired로 생성자 혹은 필드 주입
//	  @Autowired는 Spring의 의존성 주입(Dependency Injection) 어노테이션입니다. Spring이 자동으로 해당 타입의 빈(Bean)을 찾아서 주입해줍니다.

// 	  @Autowired private IdentityService identityService;
//    @Autowired (생성자가 하나만 있으면 @Autowired 생략 가능)
//    public IdentityController(IdentityService identityService) {
//        this.identityService = identityService;
//    }
	
    @GetMapping
    public ApiResponse<List<MemberResponseDto>> findAll() {
        return ApiResponse.success(memberService.findAll());
    }
    
    @GetMapping("/search")
    public ApiResponse<List<MemberResponseDto>> search(@ModelAttribute MemberRequestDto requestDto) {
        return ApiResponse.success(memberService.search(requestDto));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<MemberResponseDto> findById(@PathVariable String id) {
        return ApiResponse.success(memberService.findById(id));
    }
    
    @PostMapping
    public ApiResponse<MemberResponseDto> insert(@RequestBody MemberRequestDto requestDto) {
        return ApiResponse.success(memberService.insert(requestDto));
    }
    
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<MemberResponseDto> update(@PathVariable String id, @RequestBody MemberRequestDto requestDto) {
        return ApiResponse.success(memberService.update(id, requestDto));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        memberService.delete(id);
        return ApiResponse.success(null);
    }
}
