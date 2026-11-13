package com.ujaen.cuadramed.service;

import com.ujaen.cuadramed.excepciones.GrupoNoEncontradoExcep;
import com.ujaen.cuadramed.excepciones.GrupoYaExistenteExcep;
import com.ujaen.cuadramed.excepciones.MedicoNoEncontradoExcep;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.GrupoRepository;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioGrupo {
    @Autowired
    GrupoRepository repositorioGrupo;

    @Autowired
    UsuarioRepository repositorioUsuario;

    public void anadirGrupo(@Valid Grupo grupo){
        if(repositorioGrupo.existsByEspecialidad(grupo.getEspecialidad())){
            throw new GrupoYaExistenteExcep("Esa especialidad ya tiene un grupo existente");
        }

        repositorioGrupo.save(grupo);
    }

    public void eliminarGrupo(Long idGrupo){
        if(!repositorioGrupo.existsById(idGrupo)){
            throw new GrupoNoEncontradoExcep("No se encontro el grupo buscado");
        }
        repositorioGrupo.removeById(idGrupo);
    }

    @Transactional
    public Grupo anadirMiembro(Long idGrupo, Long idMedico) {
        Grupo grupo = repositorioGrupo.findById(idGrupo)
                .orElseThrow(() -> new GrupoNoEncontradoExcep("No se encontro el grupo buscado"));

        Usuario medico = repositorioUsuario.findById(idMedico)
                .orElseThrow(() -> new MedicoNoEncontradoExcep("No se encontro el medico buscado"));

        grupo.anadirMiembro(medico);
        return grupo;
    }

    @Transactional
    public Grupo quitarMiembro(Long idGrupo, Long idMedico) {
        Grupo grupo = repositorioGrupo.findById(idGrupo)
                .orElseThrow(() -> new GrupoNoEncontradoExcep("No se encontro el grupo buscado"));

        Usuario medico = repositorioUsuario.findById(idMedico)
                .orElseThrow(() -> new MedicoNoEncontradoExcep("No se encontro el medico buscado"));

        grupo.quitarMiembro(medico);
        return grupo;
    }
}
