package com.example.demo.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.application.member.dto.request.MemberRequestDto;
import com.example.demo.application.member.dto.response.MemberResponseDto;
import com.example.demo.application.member.mapper.MemberMapper;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.repository.jpa.MemberJpaRepository;
import com.example.demo.domain.member.repository.mybatis.MemberMybatisRepository;
import com.example.demo.domain.member.service.impl.MemberServiceImpl;
import com.example.demo.global.error.exception.EntityNotFoundException;

/**
 * MemberServiceImpl 단위 테스트.
 *
 * <p>Spring 컨텍스트 없이 Mockito 만으로 실행 — 빠름 (수 밀리초).
 * DB / Spring Bean 의존 없이 서비스 로직만 집중 검증.</p>
 *
 * <p>파일명 패턴 *Test.java → Maven Surefire 가 실행 (mvn test)</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @Mock
    private MemberMybatisRepository memberMybatisRepository;

    @Mock
    private MemberMapper memberMapper;

    // ── 테스트용 공통 픽스처 ──────────────────────────────────────────────────

    private Member buildMember() {
        return Member.builder()
                .id("user01")
                .name("홍길동")
                .email("hong@test.com")
                .build();
    }

    private MemberRequestDto buildRequestDto() {
        return MemberRequestDto.builder()
                .id("user01")
                .name("홍길동")
                .email("hong@test.com")
                .build();
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("전체 회원 조회 - 1건 반환")
    void findAll_success() {
        // given
        given(memberJpaRepository.findAll()).willReturn(List.of(buildMember()));

        // when
        List<MemberResponseDto> result = memberService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("user01");
        assertThat(result.get(0).getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("전체 회원 조회 - 데이터 없으면 빈 리스트 반환")
    void findAll_empty() {
        // given
        given(memberJpaRepository.findAll()).willReturn(List.of());

        // when
        List<MemberResponseDto> result = memberService.findAll();

        // then
        assertThat(result).isEmpty();
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("동적 검색 - 이름으로 검색 성공")
    void search_byName() {
        // given
        MemberRequestDto requestDto = MemberRequestDto.builder()
                .searchType("name")
                .searchKeyword("홍")
                .build();
        given(memberMybatisRepository.search(requestDto)).willReturn(List.of(buildMember()));

        // when
        List<MemberResponseDto> result = memberService.search(requestDto);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("홍");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("단건 조회 성공")
    void findById_success() {
        // given
        given(memberJpaRepository.findById("user01")).willReturn(Optional.of(buildMember()));

        // when
        MemberResponseDto result = memberService.findById("user01");

        // then
        assertThat(result.getId()).isEqualTo("user01");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("단건 조회 실패 - 존재하지 않는 ID → EntityNotFoundException")
    void findById_notFound() {
        // given
        given(memberJpaRepository.findById("notExist")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findById("notExist"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── insert ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("회원 등록 성공")
    void insert_success() {
        // given
        MemberRequestDto requestDto = buildRequestDto();
        Member member = buildMember();
        MemberResponseDto responseDto = MemberResponseDto.from(member);

        given(memberMapper.toEntity(requestDto)).willReturn(member);
        given(memberJpaRepository.save(member)).willReturn(member);
        given(memberMapper.toDto(member)).willReturn(responseDto);

        // when
        MemberResponseDto result = memberService.insert(requestDto);

        // then
        assertThat(result.getId()).isEqualTo("user01");
        assertThat(result.getName()).isEqualTo("홍길동");
        then(memberJpaRepository).should(times(1)).save(member);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("회원 수정 성공")
    void update_success() {
        // given
        MemberRequestDto requestDto = MemberRequestDto.builder()
                .id("user01")
                .name("수정된이름")
                .email("updated@test.com")
                .build();
        Member updatedMember = Member.builder()
                .id("user01")
                .name("수정된이름")
                .email("updated@test.com")
                .build();
        MemberResponseDto responseDto = MemberResponseDto.from(updatedMember);

        given(memberJpaRepository.existsById("user01")).willReturn(true);
        given(memberMapper.toEntity(requestDto)).willReturn(updatedMember);
        given(memberJpaRepository.save(updatedMember)).willReturn(updatedMember);
        given(memberMapper.toDto(updatedMember)).willReturn(responseDto);

        // when
        MemberResponseDto result = memberService.update("user01", requestDto);

        // then
        assertThat(result.getName()).isEqualTo("수정된이름");
        assertThat(result.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("회원 수정 실패 - 존재하지 않는 ID → EntityNotFoundException")
    void update_notFound() {
        // given
        MemberRequestDto requestDto = buildRequestDto();
        given(memberJpaRepository.existsById("user01")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.update("user01", requestDto))
                .isInstanceOf(EntityNotFoundException.class);

        then(memberJpaRepository).should(never()).save(any());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("회원 삭제 성공 - 삭제된 회원 정보 반환")
    void delete_success() {
        // given
        Member member = buildMember();
        MemberResponseDto responseDto = MemberResponseDto.from(member);

        given(memberJpaRepository.findById("user01")).willReturn(Optional.of(member));
        given(memberMapper.toDto(member)).willReturn(responseDto);
        willDoNothing().given(memberJpaRepository).delete(member);

        // when
        MemberResponseDto result = memberService.delete("user01");

        // then
        assertThat(result.getId()).isEqualTo("user01");
        then(memberJpaRepository).should(times(1)).delete(member);
    }

    @Test
    @DisplayName("회원 삭제 실패 - 존재하지 않는 ID → EntityNotFoundException")
    void delete_notFound() {
        // given
        given(memberJpaRepository.findById("notExist")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.delete("notExist"))
                .isInstanceOf(EntityNotFoundException.class);

        then(memberJpaRepository).should(never()).delete(any());
    }
}
