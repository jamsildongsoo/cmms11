package com.cmms11.file;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileGroupRepository extends JpaRepository<FileGroup, FileGroupId> {

    Optional<FileGroup> findByIdCompanyIdAndIdFileGroupId(String companyId, String fileGroupId);
}
