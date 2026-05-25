package com.example.demo.domain.member.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.domain.member.entity.Member;

/**
 * 회원 JPA 리포지토리 — 단순 CRUD / 표준 조회 전담.
 *
 * <p>설계 방침:</p>
 * <ul>
 *   <li>기본 CRUD ({@code save}, {@code findById}, {@code findAll}, {@code delete}) 는
 *       {@link JpaRepository} 가 자동 제공 — 구현 코드 불필요.</li>
 *   <li>이름 규칙 기반 메서드({@code findByEmail}) 도 Spring Data 가 자동 구현.</li>
 *   <li>간단한 JPQL 은 {@link Query @Query} 로 표현 가능 — 정적 SQL이라 가독성도 좋음.</li>
 *   <li>다중 조인 / 동적 WHERE / 통계성 쿼리는 이쪽에 두지 않고
 *       {@code MemberMybatisRepository} + XML 매퍼 사용 (SQL 자유도가 더 높음).</li>
 * </ul>
 *
 * <p>두 번째 제네릭 파라미터({@code String}) 는 {@link Member#getId()} 의 타입.</p>
 */
public interface MemberJpaRepository extends JpaRepository<Member, String> {

    /**
     * 이메일로 회원 조회 — 메서드 이름 규약으로 Spring Data 가 자동 구현한다.
     * (실제 SQL: {@code SELECT * FROM member WHERE email = ?})
     */
    Member findByEmail(String email);

    /**
     * 이름에 키워드가 포함된 회원 목록 — 간단한 JPQL 예시.
     * 보다 복잡한 동적 검색은 MyBatis 의 동적 SQL 을 권장.
     */
    @Query("SELECT m FROM Member m WHERE m.name LIKE %:keyword%")
    List<Member> searchByName(@Param("keyword") String keyword);
}
