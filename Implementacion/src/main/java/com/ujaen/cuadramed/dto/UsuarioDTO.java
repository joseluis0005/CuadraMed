package com.ujaen.cuadramed.dto;

import com.ujaen.cuadramed.model.Usuario;

public record UsuarioDTO(Long id, String nombre, String email, String telefono) {
    public static UsuarioDTO desde(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getTelefono());
    }
}
