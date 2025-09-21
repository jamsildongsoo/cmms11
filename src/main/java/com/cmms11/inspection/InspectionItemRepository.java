package com.cmms11.inspection;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionItemRepository extends JpaRepository<InspectionItem, InspectionItemId> {
    List<InspectionItem> findByIdCompanyIdAndIdInspectionIdOrderByIdLineNo(String companyId, String inspectionId);

    void deleteByIdCompanyIdAndIdInspectionId(String companyId, String inspectionId);
}
