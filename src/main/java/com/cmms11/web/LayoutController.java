package com.cmms11.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 이름: LayoutController
 * 작성자: codex
 * 작성일: 2025-01-27
 * 수정일:
 * 프로그램 개요: 레이아웃 템플릿을 제공하는 컨트롤러.
 */
@Controller
public class LayoutController {

    /**
     * 기본 레이아웃 페이지를 제공합니다.
     * 
     * @param content SPA에서 로드할 콘텐츠 URL
     * @param authentication 인증된 사용자 정보
     * @param model 뷰 모델
     * @return 레이아웃 템플릿 이름
     */
    @GetMapping("/layout/defaultLayout.html")
    public String defaultLayout(
            @RequestParam(required = false) String content,
            Authentication authentication,
            Model model) {
        
        // 기본 콘텐츠 설정
        if (content == null || content.trim().isEmpty()) {
            content = "/plant/list.html";
        }
        model.addAttribute("content", content);
        
        // 인증된 사용자 정보 추가
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            model.addAttribute("isAuthenticated", true);
            
            // 사용자명에서 회사코드와 멤버ID 분리
            String companyId = "C0001"; // 기본값
            String memberId = username;
            if (username != null && username.contains(":")) {
                String[] parts = username.split(":", 2);
                companyId = parts[0];
                memberId = parts[1];
            }
            model.addAttribute("companyId", companyId);
            model.addAttribute("memberId", memberId);
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("username", "게스트");
        }
        
        return "layout/defaultLayout";
    }
}
