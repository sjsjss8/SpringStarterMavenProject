package com.example.demo.domain.member.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.domain.member.dto.request.MemberRequestDto;
import com.example.demo.domain.member.model.Member;

@Mapper
public interface MemberMapper {
	List<Member> findAll();
	List<Member> search(MemberRequestDto requestDto);
    Member findById(String id);
    void insert(Member member);
    void update(Member member);
    void delete(String id);
}