package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionDto {
    private List<String> keywords;
    private Integer multiple_choices=0;
    private Integer N_multiple_choices=0;
    private Enum subject;
}
