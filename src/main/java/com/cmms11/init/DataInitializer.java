package com.cmms11.init;

import com.cmms11.domain.company.Company;
import com.cmms11.domain.company.CompanyRepository;
import com.cmms11.domain.dept.Dept;
import com.cmms11.domain.dept.DeptId;
import com.cmms11.domain.dept.DeptRepository;
import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberRepository;
import com.cmms11.domain.site.Site;
import com.cmms11.domain.site.SiteId;
import com.cmms11.domain.site.SiteRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String COMPANY_ID = "C0001";
    private static final String SYSTEM_USER = "system";

    private final CompanyRepository companyRepository;
    private final SiteRepository siteRepository;
    private final DeptRepository deptRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        CompanyRepository companyRepository,
        SiteRepository siteRepository,
        DeptRepository deptRepository,
        MemberRepository memberRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.companyRepository = companyRepository;
        this.siteRepository = siteRepository;
        this.deptRepository = deptRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        seedCompany(now);
        seedSites(now);
        seedDepts(now);
        seedAdminUser(now);
    }

    private void seedCompany(LocalDateTime now) {
        companyRepository
            .findById(COMPANY_ID)
            .ifPresentOrElse(
                company -> {
                    boolean changed = false;
                    if (!Objects.equals(company.getName(), "CMMS 주식회사")) {
                        company.setName("CMMS 주식회사");
                        changed = true;
                    }
                    if (!Objects.equals(company.getNote(), "기본 데모 회사")) {
                        company.setNote("기본 데모 회사");
                        changed = true;
                    }
                    if (!Objects.equals(company.getDeleteMark(), "N")) {
                        company.setDeleteMark("N");
                        changed = true;
                    }
                    if (changed) {
                        company.setUpdatedAt(now);
                        company.setUpdatedBy(SYSTEM_USER);
                        companyRepository.save(company);
                        log.info("Updated sample company {}", COMPANY_ID);
                    }
                },
                () -> {
                    Company company = new Company();
                    company.setCompanyId(COMPANY_ID);
                    company.setName("CMMS 주식회사");
                    company.setNote("기본 데모 회사");
                    company.setDeleteMark("N");
                    company.setCreatedAt(now);
                    company.setCreatedBy(SYSTEM_USER);
                    company.setUpdatedAt(now);
                    company.setUpdatedBy(SYSTEM_USER);
                    companyRepository.save(company);
                    log.info("Created sample company {}", COMPANY_ID);
                }
            );
    }

    private void seedSites(LocalDateTime now) {
        seedSite("S0001", "서울사업장", "서울시 중구 세종대로 1", now);
        seedSite("S0002", "부산사업장", "부산시 해운대구 센텀북대로 100", now);
    }

    private void seedSite(String siteId, String name, String note, LocalDateTime now) {
        SiteId id = new SiteId(COMPANY_ID, siteId);
        siteRepository
            .findById(id)
            .ifPresentOrElse(
                site -> {
                    boolean changed = false;
                    if (!Objects.equals(site.getName(), name)) {
                        site.setName(name);
                        changed = true;
                    }
                    if (!Objects.equals(site.getNote(), note)) {
                        site.setNote(note);
                        changed = true;
                    }
                    if (!Objects.equals(site.getDeleteMark(), "N")) {
                        site.setDeleteMark("N");
                        changed = true;
                    }
                    if (changed) {
                        site.setUpdatedAt(now);
                        site.setUpdatedBy(SYSTEM_USER);
                        siteRepository.save(site);
                        log.info("Updated sample site {}", siteId);
                    }
                },
                () -> {
                    Site site = new Site();
                    site.setId(id);
                    site.setName(name);
                    site.setNote(note);
                    site.setDeleteMark("N");
                    site.setCreatedAt(now);
                    site.setCreatedBy(SYSTEM_USER);
                    site.setUpdatedAt(now);
                    site.setUpdatedBy(SYSTEM_USER);
                    siteRepository.save(site);
                    log.info("Created sample site {}", siteId);
                }
            );
    }

    private void seedDepts(LocalDateTime now) {
        seedDept("D0001", "설비팀", "설비 정비 및 유지보수", null, now);
        seedDept("D0002", "안전팀", "안전 관리 및 교육", "D0001", now);
    }

    private void seedDept(String deptId, String name, String note, String parentId, LocalDateTime now) {
        DeptId id = new DeptId(COMPANY_ID, deptId);
        deptRepository
            .findById(id)
            .ifPresentOrElse(
                dept -> {
                    boolean changed = false;
                    if (!Objects.equals(dept.getName(), name)) {
                        dept.setName(name);
                        changed = true;
                    }
                    if (!Objects.equals(dept.getNote(), note)) {
                        dept.setNote(note);
                        changed = true;
                    }
                    if (!Objects.equals(dept.getParentId(), parentId)) {
                        dept.setParentId(parentId);
                        changed = true;
                    }
                    if (!Objects.equals(dept.getDeleteMark(), "N")) {
                        dept.setDeleteMark("N");
                        changed = true;
                    }
                    if (changed) {
                        dept.setUpdatedAt(now);
                        dept.setUpdatedBy(SYSTEM_USER);
                        deptRepository.save(dept);
                        log.info("Updated sample dept {}", deptId);
                    }
                },
                () -> {
                    Dept dept = new Dept();
                    dept.setId(id);
                    dept.setName(name);
                    dept.setNote(note);
                    dept.setParentId(parentId);
                    dept.setDeleteMark("N");
                    dept.setCreatedAt(now);
                    dept.setCreatedBy(SYSTEM_USER);
                    dept.setUpdatedAt(now);
                    dept.setUpdatedBy(SYSTEM_USER);
                    deptRepository.save(dept);
                    log.info("Created sample dept {}", deptId);
                }
            );
    }

    private void seedAdminUser(LocalDateTime now) {
        MemberId adminId = new MemberId(COMPANY_ID, "admin");
        if (!memberRepository.existsById(adminId)) {
            Member admin = new Member();
            admin.setId(adminId);
            admin.setName("Administrator");
            admin.setDeptId("ADMIN");
            admin.setPasswordHash(passwordEncoder.encode("1234"));
            admin.setDeleteMark("N");
            admin.setCreatedAt(now);
            admin.setCreatedBy(SYSTEM_USER);
            admin.setUpdatedAt(now);
            admin.setUpdatedBy(SYSTEM_USER);
            memberRepository.save(admin);
            log.info("Created default admin user for company {}", COMPANY_ID);
        }
    }
}

