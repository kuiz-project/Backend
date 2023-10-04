package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class TestDto {
    private Integer test_id;
    private List<QuestionDto> questions;

    public TestDto(Integer test_id){
        this.test_id=test_id;
    }
}
