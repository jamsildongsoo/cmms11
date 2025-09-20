package com.cmms11.config;

import com.cmms11.security.CsrfCookieFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * 이름: SecurityConfig
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: Spring Security 전역 설정 및 CSRF/로그 필터 구성.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(requestHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/static/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/api/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/auth/login.html")
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("member_id")
                .passwordParameter("password")
                .defaultSuccessUrl("/layout/defaultLayout.html?content=/plant/list.html", true)
                .failureUrl("/auth/login.html?error=1")
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/auth/login.html")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        http
            .addFilterBefore(new RequestLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService, PasswordEncoder encoder) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(encoder);
        return builder.build();
    }
}
