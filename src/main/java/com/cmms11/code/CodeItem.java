package com.cmms11.code;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "code_item")
@Getter
@Setter
@NoArgsConstructor
public class CodeItem {

    @EmbeddedId
    private CodeItemId id;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String note;
}

