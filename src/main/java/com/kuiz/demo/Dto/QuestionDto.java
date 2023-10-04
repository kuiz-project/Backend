package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class QuestionDto {
    private Integer question_id;  //?
    private String question;
    private String my_answer;
}
