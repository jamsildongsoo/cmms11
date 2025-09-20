package com.cmms11.domain.member;

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
public class MemberId implements Serializable {
    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "member_id", length = 5, nullable = false)
    private String memberId;
}
