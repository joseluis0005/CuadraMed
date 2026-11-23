package com.ujaen.cuadramed.excepciones;

public class UsuarioNoExisteExcep extends RuntimeException {
    public UsuarioNoExisteExcep(String message) {
        super(message);
    }
}
