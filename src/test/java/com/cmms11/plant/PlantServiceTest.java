package com.cmms11.plant;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    private PlantRepository repository;

    @InjectMocks
    private PlantService plantService;

    private Plant testPlant;
    private PlantId testPlantId;
    private PlantRequest testRequest;

    @BeforeEach
    void setUp() {
        testPlantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        testPlant = new Plant();
        testPlant.setId(testPlantId);
        testPlant.setName("테스트 설비");
        testPlant.setAssetId("A0001");
        testPlant.setSiteId("S0001");
        testPlant.setDeptId("EQ");
        testPlant.setFuncId("F0001");
        testPlant.setMakerName("테스트 제조사");
        testPlant.setSpec("테스트 스펙");
        testPlant.setModel("테스트 모델");
        testPlant.setSerial("SERIAL001");
        testPlant.setInstallDate(LocalDate.of(2024, 1, 1));
        testPlant.setDepreId("D0001");
        testPlant.setDeprePeriod(10);
        testPlant.setPurchaseCost(new BigDecimal("1000000"));
        testPlant.setResidualValue(new BigDecimal("100000"));
        testPlant.setInspectionYn("Y");
        testPlant.setPsmYn("N");
        testPlant.setWorkpermitYn("Y");
        testPlant.setInspectionInterval(30);
        testPlant.setLastInspection(LocalDate.of(2024, 1, 1));
        testPlant.setNextInspection(LocalDate.of(2024, 2, 1));
        testPlant.setFileGroupId("FG0001");
        testPlant.setNote("테스트 메모");
        testPlant.setStatus("ACTIVE");
        testPlant.setDeleteMark("N");
        testPlant.setCreatedAt(LocalDateTime.now());
        testPlant.setCreatedBy("admin");
        testPlant.setUpdatedAt(LocalDateTime.now());
        testPlant.setUpdatedBy("admin");

        testRequest = new PlantRequest(
                "P0001", "테스트 설비", "A0001", "S0001", "EQ", "F0001",
                "테스트 제조사", "테스트 스펙", "테스트 모델", "SERIAL001",
                LocalDate.of(2024, 1, 1), "D0001", 10,
                new BigDecimal("1000000"), new BigDecimal("100000"),
                "Y", "N", "Y", 30, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), "FG0001", "테스트 메모", "ACTIVE"
        );
    }

    @Test
    void list_정상조회() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plant> expectedPage = new PageImpl<>(List.of(testPlant), pageable, 1);
        when(repository.findByIdCompanyIdAndDeleteMark(MemberUserDetailsService.DEFAULT_COMPANY, "N", pageable))
                .thenReturn(expectedPage);

        // When
        Page<PlantResponse> result = plantService.list(null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("테스트 설비");
        verify(repository).findByIdCompanyIdAndDeleteMark(MemberUserDetailsService.DEFAULT_COMPANY, "N", pageable);
    }

    @Test
    void list_검색어포함조회() {
        // Given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plant> expectedPage = new PageImpl<>(List.of(testPlant), pageable, 1);
        when(repository.search(MemberUserDetailsService.DEFAULT_COMPANY, "N", "%테스트%", pageable))
                .thenReturn(expectedPage);

        // When
        Page<PlantResponse> result = plantService.list(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).search(MemberUserDetailsService.DEFAULT_COMPANY, "N", "%테스트%", pageable);
    }

    @Test
    void get_정상조회() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001")))
                .thenReturn(Optional.of(testPlant));

        // When
        PlantResponse result = plantService.get("P0001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("테스트 설비");
        verify(repository).findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001"));
    }

    @Test
    void get_존재하지않는설비() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P9999")))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> plantService.get("P9999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Plant not found: P9999");
    }

    @Test
    void create_정상생성() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001")))
                .thenReturn(Optional.empty());
        when(repository.save(any(Plant.class))).thenReturn(testPlant);

        // When
        PlantResponse result = plantService.create(testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("테스트 설비");
        assertThat(result.plantId()).isEqualTo("P0001");
        verify(repository).save(any(Plant.class));
    }

    @Test
    void create_이미존재하는설비생성시예외() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001")))
                .thenReturn(Optional.of(testPlant));

        // When & Then
        assertThatThrownBy(() -> plantService.create(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Plant already exists: P0001");
    }

    @Test
    void update_정상수정() {
        // Given
        PlantRequest updateRequest = new PlantRequest(
                "P0001", "수정된 설비", "A0001", "S0001", "EQ", "F0001",
                "수정된 제조사", "수정된 스펙", "수정된 모델", "SERIAL001",
                LocalDate.of(2024, 1, 1), "D0001", 10,
                new BigDecimal("1000000"), new BigDecimal("100000"),
                "Y", "N", "Y", 30, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), "FG0001", "수정된 메모", "ACTIVE"
        );

        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001")))
                .thenReturn(Optional.of(testPlant));
        when(repository.save(any(Plant.class))).thenReturn(testPlant);

        // When
        PlantResponse result = plantService.update("P0001", updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("수정된 설비");
        assertThat(result.makerName()).isEqualTo("수정된 제조사");
        verify(repository).save(testPlant);
    }

    @Test
    void update_존재하지않는설비수정시예외() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P9999")))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> plantService.update("P9999", testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Plant not found: P9999");
    }

    @Test
    void delete_정상삭제() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001")))
                .thenReturn(Optional.of(testPlant));
        when(repository.save(any(Plant.class))).thenReturn(testPlant);

        // When
        plantService.delete("P0001");

        // Then
        assertThat(testPlant.getDeleteMark()).isEqualTo("Y");
        verify(repository).save(testPlant);
    }

    @Test
    void delete_존재하지않는설비삭제시예외() {
        // Given
        when(repository.findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P9999")))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> plantService.delete("P9999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Plant not found: P9999");
    }
}
