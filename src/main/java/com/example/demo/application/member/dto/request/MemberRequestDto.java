package com.example.demo.application.member.dto.request;

import org.apache.ibatis.type.Alias;

import com.example.demo.global.common.dto.request.CommonRequestDto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "MemberRequestDto",
        description = "회원 요청 DTO — 등록/수정/검색 모두 이 한 DTO 로 받는다. " +
                "검색 시에는 `searchType` + `searchKeyword` (CommonRequestDto 상속) 를 사용."
)
public class MemberRequestDto extends CommonRequestDto {

    @Schema(
            description = "회원 ID (PK, 클라이언트가 직접 지정)",
            example = "user01",
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String id;

    @Schema(
            description = "회원 이름",
            example = "홍길동",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
            description = "이메일",
            example = "hong@example.com",
            maxLength = 200
    )
    private String email;
}
