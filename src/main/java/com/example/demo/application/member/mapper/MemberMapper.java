package com.example.demo.application.member.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.application.member.dto.response.MemberResponseDto;
import com.example.demo.domain.member.entity.Member;

@Mapper(componentModel = "spring")
public interface MemberMapper {
	MemberResponseDto toDto(Member member);
	Member toEntity(MemberRequestDto dto);
	
    @Mapping(target = "id", source = "id")
    @BeanMapping(ignoreByDefault = true)
	Member toEntityById(String id);
}
