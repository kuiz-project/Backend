package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class SignUpRequestDto {
    private String id;
    private String password;
    private String email;
    private String name;
}
