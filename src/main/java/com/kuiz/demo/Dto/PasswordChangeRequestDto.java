package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class PasswordChangeRequestDto {
    private String oldPassword;
    private String newPassword;

}
