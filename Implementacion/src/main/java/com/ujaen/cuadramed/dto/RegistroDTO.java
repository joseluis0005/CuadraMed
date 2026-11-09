package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Especialidad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroDTO(
        @NotBlank(message = "El nombre es obligatoria")
        String nombre,

        Especialidad especialidad,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato de email no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena,

        @NotBlank(message = "La teléfono es obligatoria")
        String telefono
) {}
