package com.ujaen.cuadramed.service;

import com.ujaen.cuadramed.excepciones.*;
import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.GrupoRepository;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioGrupo {
    @Autowired
    GrupoRepository repositorioGrupo;

    @Autowired
    UsuarioRepository repositorioUsuario;

    private void comprobarPuedeSerResponsable(Grupo grupo, Usuario usuario){
        if(repositorioGrupo.existsByResponsableId(usuario.getId())){
            throw new ResponsableYaAsignadoExcep("Ese usuario ya es responsable de otro grupo");
        }

        comprobarNoEstaEnOtroGrupo(grupo, usuario);
    }

    private void comprobarEsResponsable(Grupo grupo, String emailUsuario) {
        if (!grupo.getResponsable().getEmail().equals(emailUsuario)) {
            throw new AccesoGrupoDenegadoExcep("Solo el responsable del grupo puede gestionar sus miembros");
        }
    }

    private void comprobarNoEstaEnOtroGrupo(Grupo grupo, Usuario medico){
        if(!grupo.getMiembros().contains(medico) && repositorioGrupo.existsByMiembrosId(medico.getId())){
            throw new MedicoYaEnGrupoExcep("El medico ya pertenece a otro grupo");
        }
    }

    @Transactional
    public Grupo crearGrupo(String nombre, Especialidad especialidad, Long idResponsable){
        if(repositorioGrupo.existsByEspecialidad(especialidad)){
            throw new GrupoYaExistenteExcep("Esa especialidad ya tiene un grupo existente");
        }

        Usuario responsable = buscarUsuario(idResponsable);
        Grupo grupo = new Grupo(nombre, especialidad);

        comprobarPuedeSerResponsable(grupo, responsable);
        grupo.asignarResponsable(responsable);

        return repositorioGrupo.save(grupo);
    }

    @Transactional
    public void eliminarGrupo(Long idGrupo){
        repositorioGrupo.delete(buscarGrupo(idGrupo));
    }

    public Grupo buscarGrupo(Long idGrupo){
        return repositorioGrupo.findById(idGrupo)
                .orElseThrow(() -> new GrupoNoEncontradoExcep("No se encontro el grupo buscado"));
    }


    @Transactional
    public Grupo anadirMiembro(Long idGrupo, Long idMedico, String emailUsuario) {
        Grupo grupo = buscarGrupo(idGrupo);
        comprobarEsResponsable(grupo, emailUsuario);

        Usuario medico = buscarUsuario(idMedico);
        comprobarNoEstaEnOtroGrupo(grupo, medico);
        grupo.anadirMiembro(medico);
        return grupo;
    }

    @Transactional
    public Grupo quitarMiembro(Long idGrupo, Long idMedico, String emailUsuario) {
        Grupo grupo = buscarGrupo(idGrupo);
        comprobarEsResponsable(grupo, emailUsuario);

        Usuario medico = buscarUsuario(idMedico);
        grupo.quitarMiembro(medico);
        return grupo;
    }

    private Usuario buscarUsuario(Long idMedico) {
        return repositorioUsuario.findById(idMedico)
                .orElseThrow(() -> new MedicoNoEncontradoExcep("No se encontro el medico buscado"));
    }

    public List<Grupo> getGrupos(){
        return repositorioGrupo.findAll();
    }

    @Transactional
    public Grupo cambiarPlanificador(Long idGrupo, Long idNuevo){
        Grupo grupo = buscarGrupo(idGrupo);
        Usuario nuevoPlan = buscarUsuario(idNuevo);

        if(nuevoPlan.equals(grupo.getResponsable())){
            return grupo;
        }

        comprobarPuedeSerResponsable(grupo, nuevoPlan);
        grupo.asignarResponsable(nuevoPlan);

        return grupo;
    }


}
