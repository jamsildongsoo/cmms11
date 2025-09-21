package com.cmms11.plant;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.common.upload.BulkUploadError;
import com.cmms11.common.upload.BulkUploadResult;
import com.cmms11.common.upload.CsvUtils;
import com.cmms11.security.MemberUserDetailsService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PlantService {
    private static final String MODULE_CODE = "1";

    private final PlantRepository repository;
    private final AutoNumberService numberService;

    public PlantService(PlantRepository repository, AutoNumberService numberService) {
        this.repository = repository;
        this.numberService = numberService;
    }

    @Transactional(readOnly = true)
    public Page<Plant> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByIdCompanyIdAndDeleteMark(companyId, "N", pageable);
        }
        return repository.search(companyId, "N", "%" + q + "%", pageable);
    }

    @Transactional(readOnly = true)
    public Plant get(String plantId) {
        PlantId id = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, plantId);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Plant not found: " + plantId));
    }

    public Plant create(Plant plant) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (plant.getId() == null || plant.getId().getPlantId() == null || plant.getId().getPlantId().isBlank()) {
            // MODULE CODE for Plant(master): '1' (see docs/STRUCTURES.md)
            String newId = numberService.generateMasterId(companyId, MODULE_CODE);
            plant.setId(new PlantId(companyId, newId));
        } else {
            plant.getId().setCompanyId(companyId);
        }
        if (plant.getDeleteMark() == null) {
            plant.setDeleteMark("N");
        }
        return repository.save(plant);
    }

    public Plant update(String plantId, Plant plant) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        PlantId id = new PlantId(companyId, plantId);
        
        // 기존 Plant 존재 확인
        Plant existing = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plant not found: " + plantId));
        
        // ID 설정 (수정 시에는 기존 ID 유지)
        plant.setId(id);
        
        // 삭제 마크가 null이면 기존 값 유지
        if (plant.getDeleteMark() == null) {
            plant.setDeleteMark(existing.getDeleteMark());
        }
        
        return repository.save(plant);
    }

    public void delete(String plantId) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        PlantId id = new PlantId(companyId, plantId);

        // 기존 Plant 존재 확인
        Plant existing = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plant not found: " + plantId));

        // 소프트 삭제 (delete_mark = 'Y')
        existing.setDeleteMark("Y");
        repository.save(existing);
    }

    public BulkUploadResult upload(MultipartFile file) {
        List<BulkUploadError> errors = new ArrayList<>();
        List<Plant> pending = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        LocalDateTime now = LocalDateTime.now();
        String memberId = currentMemberId();

        try (CSVParser parser = CsvUtils.parse(file)) {
            Map<String, Integer> headerIndex = CsvUtils.normalizeHeaderMap(parser);
            CsvUtils.requireHeaders(headerIndex, List.of("name"));

            for (CSVRecord record : parser) {
                if (CsvUtils.isEmptyRecord(record)) {
                    continue;
                }
                int rowNumber = CsvUtils.displayRowNumber(record);
                try {
                    Plant plant = mapPlantRecord(record, headerIndex, companyId, now, memberId, seenIds);
                    pending.add(plant);
                    seenIds.add(plant.getId().getPlantId());
                } catch (IllegalArgumentException ex) {
                    errors.add(new BulkUploadError(rowNumber, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("CSV 파일을 읽을 수 없습니다.", ex);
        }

        if (!pending.isEmpty()) {
            repository.saveAll(pending);
        }
        return new BulkUploadResult(pending.size(), errors.size(), errors);
    }

    private Plant mapPlantRecord(
        CSVRecord record,
        Map<String, Integer> headerIndex,
        String companyId,
        LocalDateTime now,
        String memberId,
        Set<String> seenIds
    ) {
        String csvPlantId = CsvUtils.getString(record, headerIndex, "plant_id");
        if (csvPlantId != null && seenIds.contains(csvPlantId)) {
            throw new IllegalArgumentException("CSV 내에서 중복된 설비 ID입니다: " + csvPlantId);
        }

        Plant plant;
        String finalPlantId;
        if (csvPlantId == null) {
            finalPlantId = numberService.generateMasterId(companyId, MODULE_CODE);
            plant = new Plant();
            plant.setId(new PlantId(companyId, finalPlantId));
            plant.setCreatedAt(now);
            plant.setCreatedBy(memberId);
        } else {
            finalPlantId = csvPlantId;
            PlantId id = new PlantId(companyId, finalPlantId);
            Optional<Plant> existing = repository.findById(id);
            if (existing.isPresent()) {
                plant = existing.get();
                if (!"Y".equalsIgnoreCase(plant.getDeleteMark())) {
                    throw new IllegalArgumentException("이미 존재하는 설비 ID입니다: " + finalPlantId);
                }
                if (plant.getCreatedAt() == null) {
                    plant.setCreatedAt(now);
                }
                if (plant.getCreatedBy() == null) {
                    plant.setCreatedBy(memberId);
                }
            } else {
                plant = new Plant();
                plant.setId(id);
                plant.setCreatedAt(now);
                plant.setCreatedBy(memberId);
            }
        }

        plant.setName(CsvUtils.requireNonBlank(CsvUtils.getString(record, headerIndex, "name"), "name"));
        plant.setAssetId(CsvUtils.getString(record, headerIndex, "asset_id"));
        plant.setSiteId(CsvUtils.getString(record, headerIndex, "site_id"));
        plant.setDeptId(CsvUtils.getString(record, headerIndex, "dept_id"));
        plant.setFuncId(CsvUtils.getString(record, headerIndex, "func_id"));
        plant.setMakerName(CsvUtils.getString(record, headerIndex, "maker_name"));
        plant.setSpec(CsvUtils.getString(record, headerIndex, "spec"));
        plant.setModel(CsvUtils.getString(record, headerIndex, "model"));
        plant.setSerial(CsvUtils.getString(record, headerIndex, "serial"));
        plant.setInstallDate(toDate(record, headerIndex, "install_date"));
        plant.setDepreId(CsvUtils.getString(record, headerIndex, "depre_id"));
        plant.setDeprePeriod(toInteger(record, headerIndex, "depre_period"));
        plant.setPurchaseCost(toBigDecimal(record, headerIndex, "purchase_cost"));
        plant.setResidualValue(toBigDecimal(record, headerIndex, "residual_value"));
        plant.setInspectionYn(normalizeFlag(record, headerIndex, "inspection_yn"));
        plant.setPsmYn(normalizeFlag(record, headerIndex, "psm_yn"));
        plant.setWorkpermitYn(normalizeFlag(record, headerIndex, "workpermit_yn"));
        plant.setInspectionInterval(toInteger(record, headerIndex, "inspection_interval"));
        plant.setLastInspection(toDate(record, headerIndex, "last_inspection"));
        plant.setNextInspection(toDate(record, headerIndex, "next_inspection"));
        plant.setFileGroupId(CsvUtils.getString(record, headerIndex, "file_group_id"));
        plant.setNote(CsvUtils.getString(record, headerIndex, "note"));
        plant.setStatus(CsvUtils.getString(record, headerIndex, "status"));
        plant.setDeleteMark("N");
        plant.setUpdatedAt(now);
        plant.setUpdatedBy(memberId);
        return plant;
    }

    private Integer toInteger(CSVRecord record, Map<String, Integer> headerIndex, String column) {
        return CsvUtils.toInteger(CsvUtils.getString(record, headerIndex, column), column);
    }

    private BigDecimal toBigDecimal(CSVRecord record, Map<String, Integer> headerIndex, String column) {
        return CsvUtils.toBigDecimal(CsvUtils.getString(record, headerIndex, column), column);
    }

    private LocalDate toDate(CSVRecord record, Map<String, Integer> headerIndex, String column) {
        return CsvUtils.toLocalDate(CsvUtils.getString(record, headerIndex, column), column);
    }

    private String normalizeFlag(CSVRecord record, Map<String, Integer> headerIndex, String column) {
        String value = CsvUtils.getString(record, headerIndex, column);
        if (value == null) {
            return null;
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        if (!upper.equals("Y") && !upper.equals("N")) {
            throw new IllegalArgumentException(column + " 값은 Y 또는 N 이어야 합니다: " + value);
        }
        return upper;
    }

    private String currentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        String name = authentication.getName();
        return name != null ? name : "system";
    }
}
