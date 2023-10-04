package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class QuestionWithAnswerDto {
    private Integer question_id;  //?
    private String question;
    private String my_answer;
    private String answer;
    private String explan;
    private boolean correct=false;
}
