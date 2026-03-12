package edu.cit.rentuma.techloan.dto;

import java.util.Map;

public class ApiErrorResponse {
    private boolean success;
    private ErrorDetail error;
    private String timestamp;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(boolean success, ErrorDetail error, String timestamp) {
        this.success = success;
        this.error = error;
        this.timestamp = timestamp;
    }

    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class ApiErrorResponseBuilder {
        private boolean success;
        private ErrorDetail error;
        private String timestamp;

        public ApiErrorResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public ApiErrorResponseBuilder error(ErrorDetail error) {
            this.error = error;
            return this;
        }

        public ApiErrorResponseBuilder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(success, error, timestamp);
        }
    }

    public static class ErrorDetail {
        private String code;
        private String message;
        private Object details;

        public ErrorDetail() {
        }

        public ErrorDetail(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public static ErrorDetailBuilder builder() {
            return new ErrorDetailBuilder();
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getDetails() {
            return details;
        }

        public void setDetails(Object details) {
            this.details = details;
        }

        public static class ErrorDetailBuilder {
            private String code;
            private String message;
            private Object details;

            public ErrorDetailBuilder code(String code) {
                this.code = code;
                return this;
            }

            public ErrorDetailBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorDetailBuilder details(Object details) {
                this.details = details;
                return this;
            }

            public ErrorDetail build() {
                return new ErrorDetail(code, message, details);
            }
        }
    }
}
