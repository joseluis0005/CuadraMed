package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Especialidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AltaGrupoDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotNull(message = "La especialidad es obligatoria")
        Especialidad especialidad
) {}
