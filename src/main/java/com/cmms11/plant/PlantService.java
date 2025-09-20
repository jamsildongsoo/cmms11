package com.cmms11.plant;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.security.MemberUserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PlantService {
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
            String newId = numberService.generateMasterId(companyId, "1");
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
}
