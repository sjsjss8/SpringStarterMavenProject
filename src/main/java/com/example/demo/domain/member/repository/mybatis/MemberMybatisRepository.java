package com.example.demo.domain.member.repository.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.domain.member.entity.Member;

/**
 * 회원 MyBatis 리포지토리 — JPA로 표현하기 어려운 쿼리 전담.
 *
 * <p>JPA + MyBatis 공존 모델에서는 다음 경우에만 이쪽을 사용한다:</p>
 * <ul>
 *   <li>동적 WHERE 절 (검색 타입/키워드 등 런타임 결정 — {@code &lt;if test=""&gt;})</li>
 *   <li>다중 조인 + 통계 / 집계 쿼리</li>
 *   <li>네이티브 SQL 튜닝이 필요한 리포트성 쿼리</li>
 * </ul>
 *
 * <p>단순 CRUD ({@code save}, {@code findById}, {@code findByEmail} 등) 는
 * {@code MemberJpaRepository} 가 더 깔끔하니 그쪽을 권장.</p>
 *
 * <p>XML 매퍼 위치: {@code src/main/resources/static/mybatis/mapper/member/MemberMapper.xml}</p>
 */
@Mapper
public interface MemberMybatisRepository {

    /**
     * 동적 조건 검색 — 검색 타입(id/name/email)에 따라 다른 컬럼을 LIKE 매칭.
     * XML 의 {@code <choose>/<when>} 동적 SQL 로 구현.
     */
    List<Member> search(MemberRequestDto requestDto);

    /*
     * 그 외 단순 CRUD (findAll / findById / insert / update / delete) 는 JPA 가 담당하므로
     * 본 인터페이스에서는 의도적으로 제거. 필요 시 MemberJpaRepository 사용.
     */
}
