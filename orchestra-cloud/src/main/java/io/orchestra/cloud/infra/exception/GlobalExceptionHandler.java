package io.orchestra.cloud.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<StandardError> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        if (e.getMessage().contains("processamento")) {
            StandardError err = new StandardError(
                    System.currentTimeMillis(),
                    HttpStatus.CONFLICT.value(),
                    "Conflito de Idempotência",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}