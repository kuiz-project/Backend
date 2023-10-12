package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class CreateTestRequireDto {
    private Integer pdf_id;
    private Integer page;
    private Integer multiple_choices=0;
    private Integer N_multiple_choices=0;
}
