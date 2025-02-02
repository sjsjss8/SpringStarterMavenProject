package com.example.demo.application.member.dto.request;

import org.apache.ibatis.type.Alias;

import com.example.demo.global.common.dto.request.CommonRequestDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
//@AllArgsConstructor
@ToString(callSuper = true)
@Alias("MemberRequestDto")
public class MemberRequestDto extends CommonRequestDto {
	private String id;
    private String name;
    private String email;
}
