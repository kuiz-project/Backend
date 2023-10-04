package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class PdfUploadResponseDto {
    private Integer user_code;
    private PdfResponseDto pdf;
}
