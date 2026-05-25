package com.example.demo.interfaces.api.v1.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 회원 페이지 컨트롤러 (v1 — Thymeleaf 식 서버사이드 렌더링).
 *
 * <p>이 컨트롤러는 페이지 껍데기(레이아웃)만 렌더링하고, 실제 데이터 조작은 페이지 안의 JS
 * (resources/static/js/common/restApi.js) 가 v1 REST API
 * ({@code /api/v1/members/**}) 를 호출해 처리한다.</p>
 *
 * <p>경로: {@code GET /sample/members}</p>
 */
@Controller
@RequestMapping("/sample/members")
public class MemberWebController {

    @GetMapping
    public String getMemberPage() {
        return "pages/member/member";  // templates/pages/member/member.html
    }
}
