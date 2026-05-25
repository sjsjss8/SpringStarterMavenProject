package com.example.demo.application.member.dto.response;

import org.apache.ibatis.type.Alias;

import com.example.demo.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Alias("MemberResponseDto")
@Schema(
        name = "MemberResponseDto",
        description = "회원 응답 DTO — 단건/리스트 모두 동일 스키마. " +
                "`ApiResponse<T>` 의 `data` 필드로 감싸져 반환된다."
)
public class MemberResponseDto {

    @Schema(description = "회원 ID", example = "user01")
    private String id;

    @Schema(description = "회원 이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
