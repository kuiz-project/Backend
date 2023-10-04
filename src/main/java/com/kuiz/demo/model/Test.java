package com.kuiz.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer test_id;

    @Column
    private Integer score;

    @Column (nullable = false)
    private String test_name;

    @Column (nullable = false)
    private Integer multiple_choices;

    @Column (nullable = false)
    private Integer N_multiple_choices;

    @Column(columnDefinition = "JSON")
    private String questions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_code", referencedColumnName="user_code", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="pdf_id", referencedColumnName="pdf_id", nullable = false)
    private PDF pdf;

}
