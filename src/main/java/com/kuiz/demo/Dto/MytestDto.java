package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class MytestDto {
    private Integer test_id;

    private String folder_name;
    private String subject;
    private String test_name;
    private Integer page;
    private String date;
}