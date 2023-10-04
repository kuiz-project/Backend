package com.kuiz.demo.Dto;

import com.kuiz.demo.model.Folder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class FolderPdf {
    private Integer folder_id;
    private String folder_name;
    private List<PdfDto> pdfDtos;

    public FolderPdf(Folder folder) {
        this.folder_id = folder.getFolder_id();
        this.folder_name = folder.getFolder_name();
        this.pdfDtos = folder.getPdfs().stream()
                .map(PdfDto::new)
                .collect(Collectors.toList());
    }
}