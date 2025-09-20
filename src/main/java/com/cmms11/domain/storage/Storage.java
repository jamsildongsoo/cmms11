package com.cmms11.domain.storage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage")
@Getter
@Setter
@NoArgsConstructor
public class Storage {
    @EmbeddedId
    private StorageId id;

    @NotBlank
    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String note;

    @Column(name = "parent_id", length = 5)
    private String parentId;

    @Column(name = "delete_mark", length = 1)
    private String deleteMark = "N";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 10)
    private String updatedBy;
}
