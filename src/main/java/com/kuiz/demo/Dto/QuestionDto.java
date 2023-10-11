package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private String type;
    private String question;
    private List<String> choices;
    private String user_answer;
}
