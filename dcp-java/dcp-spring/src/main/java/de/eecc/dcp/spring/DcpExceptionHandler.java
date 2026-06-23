package de.eecc.dcp.spring;

import de.eecc.dcp.exception.DcpError;
import de.eecc.dcp.exception.DcpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps {@link DcpException} to HTTP responses in Spring Web applications.
 */
@RestControllerAdvice
public class DcpExceptionHandler {

    @ExceptionHandler(DcpException.class)
    public ResponseEntity<Map<String, Object>> handleDcpException(DcpException exception) {
        DcpError error = exception.error();
        return ResponseEntity
                .status(error.suggestedHttpStatus())
                .body(Map.of(
                        "error", error.getClass().getSimpleName(),
                        "message", error.message()));
    }
}
