package com.cmms11.common.seq;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sequence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sequence {
    @EmbeddedId
    private SequenceId id;

    @Column(name = "next_seq")
    private Integer nextSeq; // default 1
}

