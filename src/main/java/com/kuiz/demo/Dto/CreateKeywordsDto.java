package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class CreateKeywordsDto {
    private String pdf_url;
    private Enum subject;
}
