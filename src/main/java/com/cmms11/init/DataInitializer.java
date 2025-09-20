package com.cmms11.init;

import com.cmms11.code.CodeItem;
import com.cmms11.code.CodeItemId;
import com.cmms11.code.CodeItemRepository;
import com.cmms11.code.CodeType;
import com.cmms11.code.CodeTypeId;
import com.cmms11.code.CodeTypeRepository;
import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeTypeRepository codeTypeRepository;
    private final CodeItemRepository codeItemRepository;

    public DataInitializer(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            CodeTypeRepository codeTypeRepository,
            CodeItemRepository codeItemRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeTypeRepository = codeTypeRepository;
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // Seed admin user for company C0001 with password 1234 (BCrypt)
        MemberId adminId = new MemberId("C0001", "admin");
        if (!memberRepository.existsById(adminId)) {
            Member admin = new Member();
            admin.setId(adminId);
            admin.setName("Administrator");
            admin.setDeptId("ADMIN");
            admin.setPasswordHash(passwordEncoder.encode("1234"));
            admin.setDeleteMark("N");
            admin.setCreatedAt(now);
            admin.setCreatedBy("system");
            memberRepository.save(admin);
        }

        String companyId = "C0001";

        seedCodeType(companyId, "ASSET", "Asset Type", now);
        seedCodeType(companyId, "JOBTP", "Job Type", now);
        seedCodeType(companyId, "PERMT", "Permit Type", now);

        seedCodeItem(companyId, "ASSET", "PLANT", "Plant");
        seedCodeItem(companyId, "ASSET", "OFFIC", "Office");
        seedCodeItem(companyId, "ASSET", "INVET", "Inventory");
        seedCodeItem(companyId, "ASSET", "TOOL", "Tool");
        seedCodeItem(companyId, "ASSET", "BUILD", "Building");
        seedCodeItem(companyId, "ASSET", "ETC", "Etc");

        seedCodeItem(companyId, "JOBTP", "PLI01", "Planned Inspection");
        seedCodeItem(companyId, "JOBTP", "UPI01", "Unplanned Inspection");
        seedCodeItem(companyId, "JOBTP", "PLO01", "Planned Overhaul");
        seedCodeItem(companyId, "JOBTP", "UPO01", "Unplanned Overhaul");

        seedCodeItem(companyId, "PERMT", "P0001", "General Work Permit");
    }

    private void seedCodeType(String companyId, String codeTypeValue, String name, LocalDateTime timestamp) {
        CodeTypeId id = new CodeTypeId(companyId, codeTypeValue);
        codeTypeRepository
                .findById(id)
                .map(existing -> {
                    boolean changed = false;
                    if (!name.equals(existing.getName())) {
                        existing.setName(name);
                        changed = true;
                    }
                    if (!"N".equals(existing.getDeleteMark())) {
                        existing.setDeleteMark("N");
                        changed = true;
                    }
                    if (existing.getCreatedAt() == null) {
                        existing.setCreatedAt(timestamp);
                        changed = true;
                    }
                    if (existing.getCreatedBy() == null) {
                        existing.setCreatedBy("system");
                        changed = true;
                    }
                    if (changed) {
                        existing.setUpdatedAt(timestamp);
                        existing.setUpdatedBy("system");
                        return codeTypeRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(
                        () -> {
                            CodeType codeType = new CodeType();
                            codeType.setId(id);
                            codeType.setName(name);
                            codeType.setDeleteMark("N");
                            codeType.setCreatedAt(timestamp);
                            codeType.setCreatedBy("system");
                            codeType.setUpdatedAt(timestamp);
                            codeType.setUpdatedBy("system");
                            return codeTypeRepository.save(codeType);
                        });
    }

    private void seedCodeItem(String companyId, String codeTypeValue, String code, String name) {
        CodeItemId id = new CodeItemId(companyId, codeTypeValue, code);
        codeItemRepository
                .findById(id)
                .map(existing -> {
                    if (!name.equals(existing.getName())) {
                        existing.setName(name);
                        return codeItemRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(
                        () -> {
                            CodeItem codeItem = new CodeItem();
                            codeItem.setId(id);
                            codeItem.setName(name);
                            return codeItemRepository.save(codeItem);
                        });
    }
}

