package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;

import java.util.List;

public record GrupoDTO(Long id, String nombre, Especialidad especialidad, List<UsuarioDTO> miembros) {
    public static GrupoDTO desde(Grupo g) {
        List<UsuarioDTO> miembros = g.getMiembros().stream()
                .map(UsuarioDTO::desde)
                .toList();
        return new GrupoDTO(g.getId(), g.getNombre(), g.getEspecialidad(), miembros);
    }
}
