package com.example.demo.domain.member.entity;

import org.apache.ibatis.type.Alias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 엔티티 — JPA + MyBatis 공존 모델.
 *
 * <p>이 엔티티는 두 가지 데이터 액세스 방식에서 모두 사용된다:</p>
 * <ul>
 *   <li><b>JPA</b>: {@link jakarta.persistence.Entity @Entity} + {@link jakarta.persistence.Table @Table} 으로
 *       Hibernate가 테이블에 매핑. {@code MemberJpaRepository} 가 사용.</li>
 *   <li><b>MyBatis</b>: {@link org.apache.ibatis.type.Alias @Alias} 로 XML 매퍼에서
 *       {@code resultType="Member"} 로 짧게 참조 가능. {@code MemberMybatisRepository} 가 사용.</li>
 * </ul>
 *
 * <p>두 방식 모두 같은 DataSource 와 트랜잭션 매니저를 공유하므로
 * 한 트랜잭션 안에서 섞어 쓰더라도 정합성 문제는 없다.</p>
 */
@Entity
@Table(name = "member")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Alias("Member")
public class Member {

    /** PK. 클라이언트가 직접 지정하는 회원 ID (String). */
    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 200)
    private String email;
}
