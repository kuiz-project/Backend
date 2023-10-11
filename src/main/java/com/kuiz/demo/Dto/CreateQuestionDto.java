package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionDto {
    private List<String> keywords;
    private Integer multiple_choices;
    private Integer N_multiple_choices;
    private Enum subject;
}
