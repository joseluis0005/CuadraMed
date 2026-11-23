package com.ujaen.cuadramed.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler({GrupoNoEncontradoExcep.class, MedicoNoEnGrupoExcep.class, UsuarioNoExisteExcep.class})
    public ResponseEntity<Map<String, String>> grupoNoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({GrupoYaExistenteExcep.class, MedicoYaEnGrupoExcep.class,
            ResponsableYaAsignadoExcep.class, ExpulsionDePlanificadorExcep.class})
    public ResponseEntity<Map<String, String>> conflictoGrupo(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({UsuarioNoEsMedicoExcep.class, UsuarioNoEsPlanificadorExcep.class, MedicoNoActivoExcep.class})
    public ResponseEntity<Map<String, String>> peticionNoValidaGrupo(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AccesoGrupoDenegadoExcep.class)
    public ResponseEntity<Map<String, String>> accesoGrupoDenegado(AccesoGrupoDenegadoExcep e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Datos no validos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", mensaje));
    }
}
