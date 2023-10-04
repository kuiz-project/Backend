package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class TestWithAnswerDto {
    private Integer test_id;
    private List<QuestionWithAnswerDto> questions;

    public TestWithAnswerDto(Integer test_id){
        this.test_id=test_id;
    }

}
