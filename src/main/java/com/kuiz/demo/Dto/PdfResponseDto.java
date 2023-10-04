package com.kuiz.demo.Dto;

import com.kuiz.demo.model.Folder;
import com.kuiz.demo.model.PDF;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Optional;

@Data
public class PdfResponseDto {
    private Integer pdf_id;
    private String file_name;
    private String subject;


    public PdfResponseDto(PDF pdf) {
        this.pdf_id = pdf.getPdf_id();
        this.file_name = pdf.getFile_name();
        this.subject = pdf.getSubject();

    }
}
