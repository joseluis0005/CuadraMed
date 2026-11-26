package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;

import java.util.List;

public record GrupoDTO(Long id, String nombre, Especialidad especialidad, UsuarioDTO responsable) {
    public static GrupoDTO desde(Grupo g) {
        return new GrupoDTO(g.getId(), g.getNombre(), g.getEspecialidad(), UsuarioDTO.desde(g.getResponsable()));
    }
}
