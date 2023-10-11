package com.kuiz.demo.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuiz.demo.model.Keywords;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter(autoApply = true)
public class KeywordsConverter implements AttributeConverter<Keywords, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Keywords keywords) {
        try {
            return objectMapper.writeValueAsString(keywords);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Keywords to JSON", e);
        }
    }

    @Override
    public Keywords convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, Keywords.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to Keywords", e);
        }
    }
}