package com.ujaen.cuadramed.service;

import com.ujaen.cuadramed.excepciones.EmailYaRegistradoExcep;
import com.ujaen.cuadramed.excepciones.MedicoNoEncontradoExcep;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ServicioMedico {
    @Autowired
    private UsuarioRepository repositorioUsuario;

    public Usuario buscarMedico(Long id) {
        Usuario u = repositorioUsuario.findById(id)
                .orElseThrow(() -> new MedicoNoEncontradoExcep("id"));
        if (!u.tieneRol(Rol.MEDICO)) {
            throw new MedicoNoEncontradoExcep("id");
        }
        return u;
    }
    public void anadirMedico(@Valid Usuario medico){
        if(repositorioUsuario.existsByEmail(medico.getEmail())){
            throw new EmailYaRegistradoExcep(medico.getEmail());
        }
        repositorioUsuario.save(medico);
    }

    public List<Usuario> listarMedicos(){
        return repositorioUsuario.findByRolesContaining((Rol.MEDICO));
    }

    public List<Usuario> listarMedicosActivos(){
        return new ArrayList<>(repositorioUsuario.findByActivoTrue());
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario medico = buscarMedico(id);

        if (repositorioUsuario.existsByEmailAndId(datos.getEmail(), id)) {
            throw new EmailYaRegistradoExcep(datos.getEmail());
        }

        medico.setNombre(datos.getNombre());
        medico.setEmail(datos.getEmail());
        medico.setTelefono(datos.getTelefono());

        return medico;
    }

    public void darDeBaja(Long id) {
        buscarMedico(id).setActivo(false);
    }

}
