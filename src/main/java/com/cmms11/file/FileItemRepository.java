package com.cmms11.file;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileItemRepository extends JpaRepository<FileItem, FileItemId> {

    List<FileItem> findByIdCompanyIdAndIdFileGroupIdAndDeleteMarkNot(String companyId, String fileGroupId, String deleteMark);

    Optional<FileItem> findByIdCompanyIdAndIdFileGroupIdAndIdFileIdAndDeleteMarkNot(
        String companyId,
        String fileGroupId,
        String fileId,
        String deleteMark
    );

    @Query(
        "select coalesce(max(f.lineNo), 0) from FileItem f " +
        "where f.id.companyId = :companyId and f.id.fileGroupId = :fileGroupId"
    )
    Integer findMaxLineNo(@Param("companyId") String companyId, @Param("fileGroupId") String fileGroupId);
}
