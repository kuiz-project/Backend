package com.kuiz.demo.Dto;

import com.kuiz.demo.model.Subject;
import lombok.Data;

@Data
public class UpdatePdfSubjectDto {
    private Integer pdfId;
    private Subject pdf_subject;
}
