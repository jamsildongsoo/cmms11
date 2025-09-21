package com.cmms11.file;

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
public class FileGroupId implements Serializable {

    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "file_group_id", length = 10, nullable = false)
    private String fileGroupId;
}
