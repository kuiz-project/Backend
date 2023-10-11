package com.kuiz.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class Question {
    private String type;
    private String question;
    private List<String> choices;
    private String answer;
    private String explanation;
    private String user_answer;
    private boolean correct;
}
