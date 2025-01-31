package com.example.demo.domain.member.dto.response;


import org.apache.ibatis.type.Alias;

import com.example.demo.domain.member.model.Member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Alias("MemberResponseDto")
public class MemberResponseDto {
    private String id;
    private String name;
    private String email;
    
    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
