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
@RequiredArgsConstructor // lombok이 final 필드에 대한 생성자를 자동 생성..와 진짜 좋아졌다..
@RequestMapping("/api/v1/members")
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
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> findAll() {
        List<MemberResponseDto> members = memberService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(members));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> search(
            @ModelAttribute @Valid MemberRequestDto requestDto) {
        List<MemberResponseDto> members = memberService.search(requestDto);
        return ResponseEntity.ok(ApiResponse.ok(members));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findById(@PathVariable String id) {
        MemberResponseDto member = memberService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(member));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponseDto>> insert(
            @RequestBody @Valid MemberRequestDto requestDto) {
        MemberResponseDto savedMember = memberService.insert(requestDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(savedMember));
    }
    
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<MemberResponseDto>> update(
            @PathVariable String id, 
            @RequestBody @Valid MemberRequestDto requestDto) {
        MemberResponseDto updatedMember = memberService.update(id, requestDto);
        return ResponseEntity.ok(ApiResponse.ok(updatedMember));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> delete(@PathVariable String id) {
        MemberResponseDto deletedMember = memberService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(deletedMember));
    }
    
//    @PostMapping("/batch")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> batchInsert(
//            @RequestBody @Valid List<MemberRequestDto> requestDtos) {
//        List<MemberResponseDto> results = memberService.bulkInsert(requestDtos);
//        Map<String, Object> response = new HashMap<>();
//        response.put("totalCount", results.size());
//        response.put("results", results);
//        
//        return ResponseEntity
//            .status(HttpStatus.CREATED)
//            .body(ApiResponse.ok(response));
//    }
}
