package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Rol;
import lombok.Getter;
import lombok.Setter;

public record AuthResponseDTO (
    Long id,
    Rol rol,
    String token
){}
