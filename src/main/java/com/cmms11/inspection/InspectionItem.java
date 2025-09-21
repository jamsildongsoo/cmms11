package com.cmms11.inspection;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inspection_item")
@Getter
@Setter
@NoArgsConstructor
public class InspectionItem {

    @EmbeddedId
    private InspectionItemId id;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String method;

    @Column(name = "min_val", length = 50)
    private String minVal;

    @Column(name = "max_val", length = 50)
    private String maxVal;

    @Column(name = "std_val", length = 50)
    private String stdVal;

    @Column(length = 50)
    private String unit;

    @Column(name = "result_val", length = 50)
    private String resultVal;

    @Column(length = 500)
    private String note;
}
