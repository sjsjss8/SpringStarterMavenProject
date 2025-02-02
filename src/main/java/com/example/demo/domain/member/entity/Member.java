package com.example.demo.domain.member.entity;

import org.apache.ibatis.type.Alias;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Alias("Member")
public class Member {
    private String id;
    private String name;
    private String email;
}