package com.example.demo.domain.member.repository.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.domain.member.entity.Member;

/**
 * Mybatis를 단독으로 사용하는 경우 > Repository 또는 Mapper 네이밍 사용
 * Mybatis를 단독으로 사용하지만 MapStruct를 혼용하는 경우 또는 JPA와 Mybatis를 혼용하는 경우 > Repository 네이밍 사용
 */
@Mapper
public interface MemberMybatisRepository {
	List<Member> findAll();
	List<Member> search(MemberRequestDto requestDto);
	Member findById(String id);
	void insert(Member member);
	void update(Member member);
	void delete(Member member);
}