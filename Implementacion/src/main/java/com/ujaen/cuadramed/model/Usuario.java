package com.ujaen.cuadramed.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String telefono;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @NotBlank
    private String email;

    @NotBlank
    private String contrasena;

    //Métodos
    protected Usuario() { }

    public Usuario(String nombre, String telefono, String email, String contrasena, Rol rol) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.contrasena = contrasena;
        this.rol = rol;
    }


}