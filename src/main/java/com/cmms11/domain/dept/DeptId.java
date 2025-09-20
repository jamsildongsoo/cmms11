package com.cmms11.domain.dept;

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
public class DeptId implements Serializable {
    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "dept_id", length = 5, nullable = false)
    private String deptId;
}

