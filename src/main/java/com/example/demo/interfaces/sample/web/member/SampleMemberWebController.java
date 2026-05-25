package com.example.demo.interfaces.sample.web.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 샘플용 회원 페이지 컨트롤러 — Thymeleaf SSR 패턴 시연.
 *
 * <p>이 컨트롤러는 페이지 껍데기(레이아웃)만 렌더링하고,
 * 실제 데이터 조작은 페이지 안의 JS 가 REST API({@code /api/v1/members/**}) 를 호출해서 처리한다.</p>
 *
 * <p>운영 인터페이스는 REST API 이며, 이 페이지는 학습/예시 목적으로만 유지.</p>
 *
 * <p>경로: {@code GET /sample/members}</p>
 */
@Controller
@RequestMapping("/sample/members")
public class SampleMemberWebController {

    @GetMapping
    public String getMemberPage() {
        return "pages/member/member";  // templates/pages/member/member.html
    }
}
