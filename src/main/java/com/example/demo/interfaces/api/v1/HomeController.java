package com.example.demo.interfaces.api.v1;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈 컨트롤러 (v1 — Thymeleaf 식 서버사이드 렌더링).
 *
 * <p>이 컨트롤러는 v1 패키지 = "Thymeleaf 로 UI 를 해결하던 방식" 에 속한다.
 * SPA 기반의 새 방식(v2) 와 구분된다.</p>
 *
 * <p>경로: {@code GET /} (랜딩) · {@code GET /sample} (샘플 인덱스)</p>
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/sample"})
    public String home(Model model) {
        model.addAttribute("message", "환영합니다! (Thymeleaf 샘플 페이지 — interfaces/api/v1)");
        return "index";
    }
}
