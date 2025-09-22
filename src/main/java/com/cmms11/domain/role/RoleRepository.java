package com.cmms11.domain.role;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 이름: RoleRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 권한(Role) 엔티티 접근을 담당하는 JPA 레포지토리.
 */
public interface RoleRepository extends JpaRepository<Role, RoleId> {

    Page<Role> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query(
        """
        select r from Role r
        where r.id.companyId = :companyId
          and r.deleteMark = :deleteMark
          and (
            lower(r.id.roleId) like lower(concat('%', :keyword, '%'))
            or lower(r.name) like lower(concat('%', :keyword, '%'))
          )
        """
    )
    Page<Role> searchByKeyword(
        @Param("companyId") String companyId,
        @Param("deleteMark") String deleteMark,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Optional<Role> findByIdCompanyIdAndIdRoleId(String companyId, String roleId);
}
