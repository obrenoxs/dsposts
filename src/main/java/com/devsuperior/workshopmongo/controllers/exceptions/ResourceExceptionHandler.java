package com.devsuperior.workshopmongo.controllers.exceptions;

import com.devsuperior.workshopmongo.services.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        StandardError error = new StandardError();
        error.setError("Not found");
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        error.setStatus(status.value());
        error.setTimestamp(Instant.now());

        return ResponseEntity.status(status).body(error);
    }
}
