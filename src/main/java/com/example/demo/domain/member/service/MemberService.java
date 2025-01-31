package com.example.demo.domain.member.service;

import java.util.List;

import com.example.demo.domain.member.dto.request.MemberRequestDto;
import com.example.demo.domain.member.dto.response.MemberResponseDto;

public interface MemberService {
    List<MemberResponseDto> findAll();
    List<MemberResponseDto> search(MemberRequestDto requestDto);
    MemberResponseDto findById(String id);
    MemberResponseDto insert(MemberRequestDto requestDto);
    MemberResponseDto update(String id, MemberRequestDto requestDto);
    void delete(String id);
    
//    비즈니스 행위에 따른 메소드 네이밍 예시
//    // 기본 CRUD
//    public List<Member> findAll()
//    public Member findById(Long id)
//    public Member insert(Member member)
//    public Member update(Long id, Member member)
//    public void delete(Long id)
//    
//    // 검색 관련
//    public List<Member> search(SearchType type, String keyword)
//    public List<Member> findByNameContaining(String name)
//    public List<Member> findByEmailDomain(String domain)
//    
//    // 상태 변경
//    public void activate(Long id)
//    public void deactivate(Long id)
//    public void block(Long id)
//    public void unblock(Long id)
//    
//    // 인증/권한 관련
//    public void changePassword(Long id, String oldPassword, String newPassword)
//    public void resetPassword(Long id)
//    public void grantRole(Long id, Role role)
//    public void revokeRole(Long id, Role role)
//    
//    // 통계/집계 관련
//    public long countActiveMembers()
//    public Map<String, Long> countMembersByRole()
//    public List<Member> findTopContributors(int limit)
//    
//    // 비즈니스 로직
//    public void sendWelcomeEmail(Long id)
//    public void processWithdrawal(Long id, String reason)
//    public void updateLastLoginDate(Long id)
//    
//    // 벌크 작업
//    public List<Member> saveAll(List<Member> members)
//    public void deleteInactive(LocalDateTime beforeDate)
//    public void updateMembershipLevel(List<Long> ids, MembershipLevel level)
}
