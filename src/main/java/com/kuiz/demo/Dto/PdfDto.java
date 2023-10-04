package com.kuiz.demo.Dto;

import com.kuiz.demo.model.PDF;
import lombok.Data;

@Data
public class PdfDto {
    private Integer pdf_id;
    private String file_name;

    public PdfDto(PDF pdf) {
        this.pdf_id = pdf.getPdf_id();
        this.file_name = pdf.getFile_name();
    }
}




