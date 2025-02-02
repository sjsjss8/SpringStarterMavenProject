package com.example.demo.domain.member.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.application.member.dto.response.MemberResponseDto;
import com.example.demo.application.member.mapper.MemberMapper;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.repository.mybatis.MemberMybatisRepository;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service("MemberService")
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    
    private final MemberMybatisRepository memberRepository;
    private final MemberMapper memberMapper;
    
    @Override
    public List<MemberResponseDto> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }
    
	@Override
	public List<MemberResponseDto> search(MemberRequestDto requestDto) {
        return memberRepository.search(requestDto).stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
	}
    
    @Override
    public MemberResponseDto findById(String id) {
        Member member = memberRepository.findById(id);
        if (member == null) {
            throw new EntityNotFoundException("회원이 존재하지 않습니다.");
        }
        return MemberResponseDto.from(member);
    }
    
    @Override
    public MemberResponseDto insert(MemberRequestDto requestDto) {
    	  // 빌더패턴으로 DTO를 Entity로 변환 (생성자로 변환할 수도 있음)
//        Member member = Member.builder()
//        		.id(requestDto.getId())
//                .name(requestDto.getName())
//                .email(requestDto.getEmail())
//                .build();
//    	
//    	  memberRepository.insert(member);
//    	
//        return MemberResponseDto.from(member);
        
    	//MapStruct로 DTO를 Entity로 변환함으로써 보일러플레이트 코드 줄임
    	Member member = memberMapper.toEntity(requestDto);
    	memberRepository.insert(member);
    	
        return memberMapper.toDto(member);
    }
    
    @Override
    public MemberResponseDto update(String id, MemberRequestDto requestDto) {
//        Member member = Member.builder()
//                .id(id)
//                .name(requestDto.getName())
//                .email(requestDto.getEmail())
//                .build();
    	
    	Member member = memberMapper.toEntity(requestDto);
        memberRepository.update(member);
        
        return memberMapper.toDto(member);
    }
    
    @Override
    public MemberResponseDto delete(String id) {
//        Member member = Member.builder()
//                .id(id)
//                .build();
        
    	Member member = memberMapper.toEntityById(id);
    	
        memberRepository.delete(member);
        
        return memberMapper.toDto(member);
    }
}
