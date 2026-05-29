package edu.unac.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRuleException(BusinessRuleException ex) {
        Map<String, String> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new LinkedHashMap<>();
        String message = "Error del servidor: " + ex.getMessage();
        errorResponse.put("error", message);
        errorResponse.put("message", message);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
