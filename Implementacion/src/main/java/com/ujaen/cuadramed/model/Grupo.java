package com.ujaen.cuadramed.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    Long id;

    @NotBlank
    String nombre;

    @NotEmpty
    @OneToMany
    @JoinColumn(name = "grupo_id")
    List<Usuario> miembros;

    @NotNull
    Especialidad especialidadGrupo;
}
