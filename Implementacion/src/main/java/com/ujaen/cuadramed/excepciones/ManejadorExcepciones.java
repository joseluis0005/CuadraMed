package com.ujaen.cuadramed.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(CredencialesInvalidasExcep.class)
    public ResponseEntity<Map<String, String>> credencialesInvalidas(CredencialesInvalidasExcep e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MedicoNoEncontradoExcep.class)
    public ResponseEntity<Map<String, String>> medicoNoEncontrado(MedicoNoEncontradoExcep e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(EmailYaRegistradoExcep.class)
    public ResponseEntity<Map<String, String>> emailYaRegistrado(EmailYaRegistradoExcep e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
}