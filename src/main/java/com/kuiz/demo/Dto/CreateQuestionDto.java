package com.kuiz.demo.Dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class CreateQuestionDto {
    private List<String> keywords;
    private Integer multiple_choices;
    @JsonProperty("N_multiple_choices")
    private Integer N_multiple_choices;
    private Enum subject;
}
