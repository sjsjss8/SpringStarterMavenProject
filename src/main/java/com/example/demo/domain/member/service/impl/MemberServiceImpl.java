package com.example.demo.domain.member.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.domain.member.dto.request.MemberRequestDto;
import com.example.demo.domain.member.dto.response.MemberResponseDto;
import com.example.demo.domain.member.mapper.MemberMapper;
import com.example.demo.domain.member.model.Member;
import com.example.demo.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service("MemberService")
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    
    private final MemberMapper memberMapper;
    
    @Override
    public List<MemberResponseDto> findAll() {
        return memberMapper.findAll().stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }
    
	@Override
	public List<MemberResponseDto> search(MemberRequestDto requestDto) {
        return memberMapper.search(requestDto).stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
	}
    
    @Override
    public MemberResponseDto findById(String id) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new RuntimeException("회원이 존재하지 않습니다.");
        }
        return MemberResponseDto.from(member);
    }
    
    @Override
    public MemberResponseDto insert(MemberRequestDto requestDto) {
        Member member = Member.builder()
        		.id(requestDto.getId())
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .build();
        
        memberMapper.insert(member);
        return MemberResponseDto.from(member);
    }
    
    @Override
    public MemberResponseDto update(String id, MemberRequestDto requestDto) {
        Member member = Member.builder()
                .id(id)
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .build();
        
        memberMapper.update(member);
        return MemberResponseDto.from(member);
    }
    
    @Override
    public void delete(String id) {
        memberMapper.delete(id);
    }
}
