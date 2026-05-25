package com.example.demo.domain.member.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.application.member.dto.response.MemberResponseDto;
import com.example.demo.application.member.mapper.MemberMapper;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.repository.jpa.MemberJpaRepository;
import com.example.demo.domain.member.repository.mybatis.MemberMybatisRepository;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * 회원 서비스 — JPA + MyBatis 공존 사용 예시.
 *
 * <h3>역할 분리 가이드</h3>
 * <ul>
 *   <li><b>JPA ({@link MemberJpaRepository}) 우선</b> — 기본 CRUD, 단순 조회, PK 기반 lookup</li>
 *   <li><b>MyBatis ({@link MemberMybatisRepository}) 보완</b> — 동적 WHERE, 다중 조인, 통계/리포트 쿼리</li>
 * </ul>
 *
 * <p>두 방식은 같은 트랜잭션 안에서 자유롭게 섞어 써도 된다 — Spring 의 트랜잭션 매니저가
 * JPA 의 EntityManager 와 MyBatis 의 SqlSession 을 모두 같은 커넥션 위에 묶어 준다.</p>
 */
@Service("MemberService")
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    /** 기본 CRUD 용 — Spring Data 가 자동 구현 */
    private final MemberJpaRepository memberJpaRepository;

    /** 동적 검색 / 복잡 쿼리 용 — XML 매퍼에 SQL 직접 작성 */
    private final MemberMybatisRepository memberMybatisRepository;

    private final MemberMapper memberMapper;

    /** 전체 조회 — JPA 의 기본 메서드 사용 */
    @Override
    @Transactional(readOnly = true)
    public List<MemberResponseDto> findAll() {
        return memberJpaRepository.findAll().stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 동적 조건 검색 — MyBatis 사용.
     * <p>검색 타입(id/name/email)과 키워드가 런타임에 결정되므로
     * XML {@code <if test="">} 동적 SQL 이 가독성/유지보수 면에서 유리하다.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<MemberResponseDto> search(MemberRequestDto requestDto) {
        return memberMybatisRepository.search(requestDto).stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }

    /** PK 단건 조회 — JPA 의 Optional 패턴 사용 */
    @Override
    @Transactional(readOnly = true)
    public MemberResponseDto findById(String id) {
        Member member = memberJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        return MemberResponseDto.from(member);
    }

    /** 등록 — JPA save() 사용 */
    @Override
    public MemberResponseDto insert(MemberRequestDto requestDto) {
        Member member = memberMapper.toEntity(requestDto);
        Member saved = memberJpaRepository.save(member);
        return memberMapper.toDto(saved);
    }

    /** 수정 — 존재 여부 확인 후 새 인스턴스로 교체 저장 */
    @Override
    public MemberResponseDto update(String id, MemberRequestDto requestDto) {
        // Member 가 불변 객체이므로 dirty checking 대신 존재 검증 + 새 인스턴스 save 방식.
        // 필요 시 Member 에 setter 를 추가하면 영속 엔티티를 fetch 해서 변경하는 패턴으로 전환 가능.
        if (!memberJpaRepository.existsById(id)) {
            throw new EntityNotFoundException("회원이 존재하지 않습니다.");
        }
        Member updated = memberMapper.toEntity(requestDto);
        return memberMapper.toDto(memberJpaRepository.save(updated));
    }

    /** 삭제 — JPA deleteById 사용 */
    @Override
    public MemberResponseDto delete(String id) {
        Member member = memberJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        memberJpaRepository.delete(member);
        return memberMapper.toDto(member);
    }
}
