package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class MytestDto {
    private Integer test_id;

    private Enum subject;
    private String file_name;

    private String folder_name;

    private Integer page;
    private String date;
    private String score;
}
