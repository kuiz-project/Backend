package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class ScoreTestDto {
    private List<ScoreQuestionDto> questions;
}
