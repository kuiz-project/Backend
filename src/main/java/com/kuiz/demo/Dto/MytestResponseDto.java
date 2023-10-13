package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class MytestResponseDto {
    private List<MytestDto> tests;
}
