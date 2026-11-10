package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Rol;

import java.util.Set;

public record RespuestaAutenticacionDTO(
    Long id,
    Set<Rol> rol,
    String token
){}
