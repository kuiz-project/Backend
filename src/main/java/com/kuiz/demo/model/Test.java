package com.kuiz.demo.model;

import com.kuiz.demo.Converter.QuestionDataConverter;
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
    private Integer multiple_choices=0;

    @Column (nullable = false)
    private Integer N_multiple_choices=0;

    @Column (nullable = false)
    private String date;

    @Column(columnDefinition = "JSON")
    @Convert(converter = QuestionDataConverter.class)
    private QuestionData questionData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_code", referencedColumnName="user_code", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="pdf_id", referencedColumnName="pdf_id", nullable = false)
    private PDF pdf;

}
