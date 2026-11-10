package com.ujaen.cuadramed.controladores;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RegistroDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.dto.UsuarioDTO;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.service.ServicioAutenticacion;
import com.ujaen.cuadramed.service.ServicioMedico;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/medico")
public class ControladorMedico {
    @Autowired
    ServicioAutenticacion servicioAutenticacion;

    @Autowired
    ServicioMedico servicioMedico;

    @PostMapping("/alta")
    public RespuestaAutenticacionDTO altaMedico(@Valid @RequestBody RegistroDTO peticion){
        Usuario usuarioARegistrar = servicioAutenticacion.registro(peticion);

        servicioMedico.anadirMedico(usuarioARegistrar);

        usuarioARegistrar.setContrasena(peticion.contrasena());
        LoginDTO nuevoLogin = new LoginDTO(usuarioARegistrar.getEmail(), usuarioARegistrar.getContrasena());

        return servicioAutenticacion.login(nuevoLogin);
    }

    @GetMapping
    public List<UsuarioDTO> listarMedicos() {
        return servicioMedico.listarMedicos().stream()
                .map(UsuarioDTO::desde)
                .toList();
    }

    @PutMapping("/{id}/baja")
    public ResponseEntity<Void> bajaMedico(@PathVariable Long id){
        servicioMedico.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}