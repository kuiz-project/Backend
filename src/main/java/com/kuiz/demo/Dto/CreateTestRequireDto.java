package com.kuiz.demo.Dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CreateTestRequireDto {
    private Integer pdf_id;
    private Integer page;
    private Integer multiple_choices = 0;
    @JsonProperty("N_multiple_choices")
    private Integer N_multiple_choices = 0;
}
