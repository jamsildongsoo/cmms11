package com.cmms11.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

/**
 * 이름: CsrfCookieFilter
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: Spring Security가 발급한 CSRF 토큰을 읽어 XSRF-TOKEN 쿠키로 동기화하는 필터.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            Cookie existing = WebUtils.getCookie(request, CSRF_COOKIE_NAME);
            String token = csrfToken.getToken();
            if (token != null && (existing == null || !token.equals(existing.getValue()))) {
                Cookie cookie = new Cookie(CSRF_COOKIE_NAME, token);
                cookie.setPath("/");
                cookie.setSecure(request.isSecure());
                cookie.setHttpOnly(false);
                response.addCookie(cookie);
            }
        }
        filterChain.doFilter(request, response);
    }
}

