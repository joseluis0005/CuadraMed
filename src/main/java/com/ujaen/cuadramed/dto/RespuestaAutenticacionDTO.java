package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Rol;

public record RespuestaAutenticacionDTO(
    Long id,
    Rol rol,
    String token
){}
