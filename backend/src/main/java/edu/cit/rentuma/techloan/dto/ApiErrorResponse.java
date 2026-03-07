package edu.cit.rentuma.techloan.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApiErrorResponse {
    private boolean success;
    private ErrorDetail error;
    private String timestamp;

    @Data
    @Builder
    public static class ErrorDetail {
        private String code;
        private String message;
        private Object details;
    }
}
