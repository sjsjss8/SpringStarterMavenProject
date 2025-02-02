package com.example.demo.domain.member.service;

import java.util.List;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.application.member.dto.response.MemberResponseDto;

public interface MemberService {
    List<MemberResponseDto> findAll();
    List<MemberResponseDto> search(MemberRequestDto requestDto);
    MemberResponseDto findById(String id);
    MemberResponseDto insert(MemberRequestDto requestDto);
    MemberResponseDto update(String id, MemberRequestDto requestDto);
    MemberResponseDto delete(String id);
}
