package com.cmms11.common.seq;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SequenceId implements Serializable {
    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "module_code", length = 1, nullable = false)
    private String moduleCode;

    @Column(name = "date_key", length = 6, nullable = false)
    private String dateKey; // YYMMDD for transactions, '000000' for master
}

