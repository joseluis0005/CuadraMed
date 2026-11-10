package com.ujaen.cuadramed.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
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

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "rol")
    @NotEmpty
    private Set<Rol> roles = new HashSet<>();

    @NotBlank
    private String email;

    @NotBlank
    private String contrasena;

    @Column(nullable = false)
    private boolean activo;

    //Métodos
    protected Usuario() { }

    public Usuario(String nombre, String telefono, String email, String contrasena, Set<Rol> roles) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.contrasena = contrasena;
        this.roles = new HashSet<>(roles);
        this.activo = true;
    }

    public boolean tieneRol(Rol rol) { return roles.contains(rol); }
}