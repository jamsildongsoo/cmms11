package com.cmms11.common.seq;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AutoNumberService {
    private final SequenceRepository repository;

    public AutoNumberService(SequenceRepository repository) {
        this.repository = repository;
    }

    // Master ID: {moduleCode(1)}{9-digit seq}, dateKey fixed to '000000'
    @Transactional
    public String generateMasterId(String companyId, String moduleCode) {
        String dateKey = "000000";
        int seq = getNextSequence(companyId, moduleCode, dateKey);
        return moduleCode + String.format("%09d", seq);
    }

    // Transactional ID: {moduleCode(1)}{YYMMDD}{3-digit seq}
    @Transactional
    public String generateTxId(String companyId, String moduleCode, LocalDate date) {
        LocalDate dateKey = date == null ? LocalDate.now() : date;
        String yymmdd = String.format("%1$ty%1$tm%1$td", dateKey);
        int seq = getNextSequence(companyId, moduleCode, yymmdd);
        return moduleCode + yymmdd + String.format("%03d", seq);
    }

    @Transactional
    public int getNextSequence(String companyId, String moduleCode, String dateKey) {
        Sequence seq = repository.findForUpdate(companyId, moduleCode, dateKey)
            .orElseGet(() -> {
                Sequence s = new Sequence();
                s.setId(new SequenceId(companyId, moduleCode, dateKey));
                s.setNextSeq(1); // TABLES.md default
                return repository.save(s);
            });
        int next = (seq.getNextSeq() == null ? 1 : seq.getNextSeq());
        seq.setNextSeq(next + 1);
        repository.save(seq);
        return next;
    }
}
