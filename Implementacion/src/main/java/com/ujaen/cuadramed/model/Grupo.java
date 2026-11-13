package com.ujaen.cuadramed.model;

import com.ujaen.cuadramed.excepciones.MedicoNoEnGrupoExcep;
import com.ujaen.cuadramed.excepciones.MedicoYaEnGrupoExcep;
import com.ujaen.cuadramed.excepciones.UsuarioNoEsMedicoExcep;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

    @OneToMany
    @JoinColumn(name = "grupo_id")
    List<Usuario> miembros = new ArrayList<>();

    @NotNull
    Especialidad especialidad;

    public Grupo(){}

    public Grupo(String nombre,Especialidad especialidad){
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public void anadirMiembro(@Valid Usuario medico) {
        if (!medico.getRoles().contains(Rol.MEDICO)) {
            throw new UsuarioNoEsMedicoExcep("El usuario que se quiere introducir en el grupo no es medico");
        }

        if (miembros.contains(medico)) {
            throw new MedicoYaEnGrupoExcep("El medico ya esta en el grupo");
        }

        miembros.add(medico);
    }

    public void quitarMiembro(Usuario medico) {
        if (!miembros.remove(medico)) {
            throw new MedicoNoEnGrupoExcep("El medico no pertenece al grupo");
        }
    }
}
