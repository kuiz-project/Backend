package com.kuiz.demo.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Keywords {
    private Map<String, List<String>> pageKeywords;
}
