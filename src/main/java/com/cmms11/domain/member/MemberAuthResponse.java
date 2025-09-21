package com.cmms11.domain.member;

import java.util.List;

/**
 * 인증된 사용자의 최소 정보를 응답하기 위한 DTO.
 */
public record MemberAuthResponse(
    String memberId,
    String companyId,
    List<String> roles,
    String name,
    String deptId,
    String email
) {

    public MemberAuthResponse {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public static MemberAuthResponse of(String memberId, String companyId, List<String> roles, Member member) {
        String resolvedName = null;
        String resolvedDeptId = null;
        String resolvedEmail = null;
        if (member != null) {
            resolvedName = member.getName();
            resolvedDeptId = member.getDeptId();
            resolvedEmail = member.getEmail();
        }
        return new MemberAuthResponse(memberId, companyId, roles, resolvedName, resolvedDeptId, resolvedEmail);
    }
}
