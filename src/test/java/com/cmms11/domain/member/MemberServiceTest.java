package com.cmms11.domain.member;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private MemberId testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        testMember = new Member();
        testMember.setId(testMemberId);
        testMember.setName("테스트 사용자");
        testMember.setDeptId("EQ");
        testMember.setEmail("test@example.com");
        testMember.setPhone("010-1234-5678");
        testMember.setSiteId("S0001");
        testMember.setNote("테스트 메모");
        testMember.setDeleteMark("N");
        testMember.setCreatedAt(LocalDateTime.now());
        testMember.setCreatedBy("admin");
        testMember.setUpdatedAt(LocalDateTime.now());
        testMember.setUpdatedBy("admin");
    }

    @Test
    void list_정상조회() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> expectedPage = new PageImpl<>(List.of(testMember), pageable, 1);
        when(repository.findByIdCompanyIdAndDeleteMark(MemberUserDetailsService.DEFAULT_COMPANY, "N", pageable))
                .thenReturn(expectedPage);

        // When
        Page<Member> result = memberService.list(null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 사용자");
        verify(repository).findByIdCompanyIdAndDeleteMark(MemberUserDetailsService.DEFAULT_COMPANY, "N", pageable);
    }

    @Test
    void list_검색어포함조회() {
        // Given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> expectedPage = new PageImpl<>(List.of(testMember), pageable, 1);
        when(repository.search(MemberUserDetailsService.DEFAULT_COMPANY, "N", "%테스트%", pageable))
                .thenReturn(expectedPage);

        // When
        Page<Member> result = memberService.list(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).search(MemberUserDetailsService.DEFAULT_COMPANY, "N", "%테스트%", pageable);
    }

    @Test
    void get_정상조회() {
        // Given
        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01"))
                .thenReturn(Optional.of(testMember));

        // When
        Member result = memberService.get("test01");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트 사용자");
        verify(repository).findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
    }

    @Test
    void get_존재하지않는사용자() {
        // Given
        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "nonexistent"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.get("nonexistent"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Member not found: nonexistent");
    }

    @Test
    void create_정상생성() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encoded_password_123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(repository.save(any(Member.class))).thenReturn(testMember);

        // When
        Member result = memberService.create(testMember, rawPassword, "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(result.getId().getCompanyId()).isEqualTo(MemberUserDetailsService.DEFAULT_COMPANY);
        assertThat(result.getDeleteMark()).isEqualTo("N");
        verify(passwordEncoder).encode(rawPassword);
        verify(repository).save(testMember);
    }

    @Test
    void create_비밀번호없이생성() {
        // Given
        when(repository.save(any(Member.class))).thenReturn(testMember);

        // When
        Member result = memberService.create(testMember, null, "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPasswordHash()).isNull();
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository).save(testMember);
    }

    @Test
    void create_memberId없이생성시예외() {
        // Given
        testMember.setId(null);

        // When & Then
        assertThatThrownBy(() -> memberService.create(testMember, "password", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("member.id (memberId) is required");
    }

    @Test
    void update_정상수정() {
        // Given
        Member existingMember = new Member();
        existingMember.setId(testMemberId);
        existingMember.setName("기존 사용자");
        existingMember.setDeptId("AS");
        existingMember.setEmail("old@example.com");
        existingMember.setPhone("010-9999-9999");
        existingMember.setSiteId("S0002");
        existingMember.setNote("기존 메모");
        existingMember.setDeleteMark("N");

        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01"))
                .thenReturn(Optional.of(existingMember));
        when(repository.save(any(Member.class))).thenReturn(existingMember);

        // When
        Member result = memberService.update(testMember, null, "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트 사용자");
        assertThat(result.getDeptId()).isEqualTo("EQ");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPhone()).isEqualTo("010-1234-5678");
        assertThat(result.getSiteId()).isEqualTo("S0001");
        assertThat(result.getNote()).isEqualTo("테스트 메모");
        verify(repository).save(existingMember);
    }

    @Test
    void update_비밀번호포함수정() {
        // Given
        String rawPassword = "newpassword123";
        String encodedPassword = "encoded_new_password";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01"))
                .thenReturn(Optional.of(testMember));
        when(repository.save(any(Member.class))).thenReturn(testMember);

        // When
        Member result = memberService.update(testMember, rawPassword, "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPasswordHash()).isEqualTo(encodedPassword);
        verify(passwordEncoder).encode(rawPassword);
        verify(repository).save(testMember);
    }

    @Test
    void update_존재하지않는사용자수정시예외() {
        // Given
        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "nonexistent"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.update(testMember, null, "admin"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Member not found: nonexistent");
    }

    @Test
    void delete_정상삭제() {
        // Given
        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01"))
                .thenReturn(Optional.of(testMember));
        when(repository.save(any(Member.class))).thenReturn(testMember);

        // When
        memberService.delete("test01", "admin");

        // Then
        assertThat(testMember.getDeleteMark()).isEqualTo("Y");
        verify(repository).save(testMember);
    }

    @Test
    void delete_존재하지않는사용자삭제시예외() {
        // Given
        when(repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, "nonexistent"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.delete("nonexistent", "admin"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Member not found: nonexistent");
    }
}
