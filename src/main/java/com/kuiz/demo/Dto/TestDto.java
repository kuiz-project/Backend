package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class TestDto {
    private Integer test_id;
    private List<QuestionDto> questions;


}
