package com.kuiz.demo.Dto;

import com.kuiz.demo.model.User;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserPdfDto {
    private Integer user_code;
    private List<PdfResponseDto> pdf;

    public UserPdfDto (User user){
        this.user_code = user.getUser_code();
        this.pdf = user.getPdfs().stream().map(PdfResponseDto::new).collect(Collectors.toList());
    }
}
