package com.example.demo.interfaces.web.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/members")
public class MemberController {
    
    @GetMapping
    public String getMemberPage() {
        return "pages/member/member";  // templates/pages/member/member.html
    }
}
