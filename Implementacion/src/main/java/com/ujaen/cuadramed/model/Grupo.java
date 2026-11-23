package com.ujaen.cuadramed.model;

import com.ujaen.cuadramed.excepciones.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @OneToOne
    @JoinColumn(name = "responsable_id", unique = true)
    @Setter(AccessLevel.NONE)
    Usuario responsable;

    @NotNull
    @Enumerated(EnumType.STRING)
    Especialidad especialidad;

    public Grupo(){}

    public Grupo(String nombre,Especialidad especialidad, Usuario responsable){
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.responsable = responsable;
    }

    public void anadirMiembro(@Valid Usuario medico) {
        if (!medico.getRoles().contains(Rol.MEDICO)) {
            throw new UsuarioNoEsMedicoExcep("El usuario que se quiere introducir en el grupo no es medico");
        }

        if (miembros.contains(medico)) {
            throw new MedicoYaEnGrupoExcep("El medico ya esta en el grupo");
        }

        if(!medico.isActivo()){
            throw new MedicoNoActivoExcep("El medico no esta activo y no se puede añadir al grupo");
        }

        miembros.add(medico);
    }

    public void quitarMiembro(@Valid Usuario medico) {
        if(medico.equals(responsable)){
            throw new ExpulsionDePlanificadorExcep("No se puede expulsar del grupo al planificador");
        }

        if (!miembros.remove(medico)) {
            throw new MedicoNoEnGrupoExcep("El medico no pertenece al grupo");
        }
    }

    public void asignarResponsable(@Valid Usuario responsableP){
        if(!responsableP.getRoles().contains(Rol.PLANIFICADOR)){
            throw new UsuarioNoEsPlanificadorExcep("El usuario no es un planificador");
        }

        if(!miembros.contains(responsableP)){
            anadirMiembro(responsableP);
        }

        responsable = responsableP;
    }
}
