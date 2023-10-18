package com.kuiz.demo.Dto;

import lombok.Data;

import java.util.List;

@Data
public class ScoreTestListResponse {
    List<ScoreTestRequestDto> questions;
}
