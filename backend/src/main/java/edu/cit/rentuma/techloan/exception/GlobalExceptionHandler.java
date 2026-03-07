package edu.cit.rentuma.techloan.exception;

import edu.cit.rentuma.techloan.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Validation errors (@Valid) ───────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                    (a, b) -> a
                ));

        return ResponseEntity.badRequest().body(
            error("VALID-001", "Validation failed", fieldErrors));
    }

    // ── Duplicate / business rule violations ────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(IllegalStateException ex) {
        String[] parts = ex.getMessage().split(":", 2);
        String code = parts.length == 2 ? parts[0] : "DB-002";
        String msg  = parts.length == 2 ? parts[1] : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(code, msg, null));
    }

    // ── Validation rule violations ───────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        String[] parts = ex.getMessage().split(":", 2);
        String code = parts.length == 2 ? parts[0] : "VALID-001";
        String msg  = parts.length == 2 ? parts[1] : ex.getMessage();
        return ResponseEntity.badRequest().body(error(code, msg, null));
    }

    // ── Invalid credentials ──────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        String[] parts = ex.getMessage().split(":", 2);
        String code = parts.length == 2 ? parts[0] : "AUTH-001";
        String msg  = parts.length == 2 ? parts[1] : "Invalid credentials";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(code, msg, null));
    }

    // ── Not found ────────────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex) {
        String[] parts = ex.getMessage().split(":", 2);
        String code = parts.length == 2 ? parts[0] : "SYSTEM-001";
        String msg  = parts.length == 2 ? parts[1] : ex.getMessage();
        HttpStatus status = code.startsWith("DB-001") ? HttpStatus.NOT_FOUND
                          : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(error(code, msg, null));
    }

    // ── Builder ──────────────────────────────────────────
    private ApiErrorResponse error(String code, String message, Object details) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(ApiErrorResponse.ErrorDetail.builder()
                        .code(code)
                        .message(message.trim())
                        .details(details)
                        .build())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
