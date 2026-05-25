package com.example.demo.interfaces.sample.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 샘플용 홈 컨트롤러 — Thymeleaf SSR 패턴 시연.
 *
 * <p>이 프로젝트의 메인 인터페이스는 REST API ({@code /api/v1/**}) 이며,
 * Thymeleaf 는 학습/예시용 샘플 페이지에서만 사용한다.</p>
 *
 * <p>경로: {@code GET /} (랜딩) · {@code GET /sample} (샘플 인덱스)</p>
 */
@Controller
public class SampleHomeController {

    @GetMapping({"/", "/sample"})
    public String home(Model model) {
        model.addAttribute("message", "환영합니다! (Thymeleaf 샘플 페이지)");
        return "index";
    }
}
