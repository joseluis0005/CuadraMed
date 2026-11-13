package com.ujaen.cuadramed.service;

import com.ujaen.cuadramed.excepciones.GrupoNoEncontradoExcep;
import com.ujaen.cuadramed.excepciones.GrupoYaExistenteExcep;
import com.ujaen.cuadramed.excepciones.MedicoNoEncontradoExcep;
import com.ujaen.cuadramed.excepciones.MedicoYaEnGrupoExcep;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.GrupoRepository;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioGrupo {
    @Autowired
    GrupoRepository repositorioGrupo;

    @Autowired
    UsuarioRepository repositorioUsuario;

    public Grupo anadirGrupo(Grupo grupo){
        if(repositorioGrupo.existsByEspecialidad(grupo.getEspecialidad())){
            throw new GrupoYaExistenteExcep("Esa especialidad ya tiene un grupo existente");
        }

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
    public Grupo anadirMiembro(Long idGrupo, Long idMedico) {
        Grupo grupo = buscarGrupo(idGrupo);
        Usuario medico = buscarUsuario(idMedico);

        // Un médico solo puede pertenecer a un grupo
        if (!grupo.getMiembros().contains(medico) && repositorioGrupo.existsByMiembrosId(idMedico)) {
            throw new MedicoYaEnGrupoExcep("El medico ya pertenece a otro grupo");
        }

        grupo.anadirMiembro(medico);
        return grupo;
    }

    @Transactional
    public Grupo quitarMiembro(Long idGrupo, Long idMedico) {
        Grupo grupo = buscarGrupo(idGrupo);
        Usuario medico = buscarUsuario(idMedico);

        grupo.quitarMiembro(medico);
        return grupo;
    }

    private Usuario buscarUsuario(Long idMedico) {
        return repositorioUsuario.findById(idMedico)
                .orElseThrow(() -> new MedicoNoEncontradoExcep("No se encontro el medico buscado"));
    }
}
