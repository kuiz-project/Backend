package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class CreateTestDto {
    private Integer pdf_id;
    private Integer page;
    private Integer multiple_choices;
    private Integer N_multiple_choices;
}
