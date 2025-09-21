package com.cmms11.plant;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.common.upload.BulkUploadResult;
import com.cmms11.security.MemberUserDetailsService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

@DataJpaTest
@Import({PlantService.class, AutoNumberService.class})
class PlantServiceUploadTest {

    @Autowired
    private PlantService plantService;

    @Autowired
    private PlantRepository plantRepository;

    @Test
    void uploadCreatesMultiplePlants() throws Exception {
        String csv = String.join(
            "\n",
            "plant_id,name,asset_id,site_id,dept_id,func_id,maker_name,spec,model,serial,install_date,depre_id,depre_period,purchase_cost,residual_value,inspection_yn,psm_yn,workpermit_yn,inspection_interval,last_inspection,next_inspection,file_group_id,note,status",
            ",순환 펌프,EQPMT,SEOUL,EQ,PMP,Acme,용량 40m3/h,M-100,SN-1001,2024-01-15,SL,10,12000000,1000000,Y,N,Y,12,2024-07-01,2025-07-01,FG0001,예비 펌프,ACTIVE",
            "PLT9000001,에어 컴프레서,EQPMT,BUSAN,EQ,CMP,Atlas,,GA15,SN-2002,2023-06-10,SL,8,15000000,1200000,N,N,N,6,2024-05-10,2024-11-10,,비고 없음,ACTIVE"
        );
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "plants.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        BulkUploadResult result = plantService.upload(file);

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failureCount()).isZero();

        List<Plant> plants = plantRepository.findAll();
        assertThat(plants).hasSize(2);
        Plant generated = plants.stream()
            .filter(p -> "순환 펌프".equals(p.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(generated.getId().getCompanyId()).isEqualTo(MemberUserDetailsService.DEFAULT_COMPANY);
        assertThat(generated.getId().getPlantId()).startsWith("1");
        assertThat(generated.getDeleteMark()).isEqualTo("N");

        Plant manual = plantRepository
            .findById(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "PLT9000001"))
            .orElseThrow();
        assertThat(manual.getName()).isEqualTo("에어 컴프레서");
        assertThat(manual.getDeleteMark()).isEqualTo("N");
    }

    @Test
    void uploadCollectsRowErrorsWithoutStoppingProcessing() throws Exception {
        Plant existing = new Plant();
        existing.setId(new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "PLT9000002"));
        existing.setName("기존 설비");
        existing.setDeleteMark("N");
        plantRepository.save(existing);

        String csv = String.join(
            "\n",
            "plant_id,name,asset_id,dept_id,install_date",
            "PLT9000002,중복 설비,EQPMT,EQ,2024-01-10",
            ",,EQPMT,EQ,2024-01-11",
            ",펌프 A,EQPMT,EQ,2024-02-01"
        );
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "plants-errors.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        BulkUploadResult result = plantService.upload(file);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(2);
        assertThat(result.errors())
            .extracting("rowNumber")
            .containsExactlyInAnyOrder(2, 3);
        assertThat(result.errors())
            .extracting("message")
            .anyMatch(msg -> msg.contains("이미 존재하는 설비 ID"));
        assertThat(result.errors())
            .extracting("message")
            .anyMatch(msg -> msg.contains("필수 값이 비어 있습니다"));

        Plant saved = plantRepository
            .findAll()
            .stream()
            .filter(p -> "펌프 A".equals(p.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(saved.getDeleteMark()).isEqualTo("N");
    }
}
