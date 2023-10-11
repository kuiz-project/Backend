package com.kuiz.demo.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuiz.demo.model.QuestionData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter(autoApply = true)
public class QuestionDataConverter implements AttributeConverter<QuestionData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(QuestionData questionData) {
        try {
            return objectMapper.writeValueAsString(questionData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert QuestionData to JSON", e);
        }
    }

    @Override
    public QuestionData convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, QuestionData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to QuestionData", e);
        }
    }
}