package com.kuiz.demo.Dto;

import lombok.Data;

@Data
public class DeletePDFsResult {
    private boolean success;
    private String message;

    public DeletePDFsResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
