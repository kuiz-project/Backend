package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class ScoreQuestionDto {
    private Integer sequence;
    private String question;
    private String answer;
    private String user_answer;
}