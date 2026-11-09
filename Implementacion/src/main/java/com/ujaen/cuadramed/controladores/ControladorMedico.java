package com.ujaen.cuadramed.controladores;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RegistroDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.service.ServicioAutenticacion;
import com.ujaen.cuadramed.service.ServicioMedico;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medico")
public class ControladorMedico {
    @Autowired
    ServicioAutenticacion servicioAutenticacion;

    @Autowired
    ServicioMedico servicioMedico;

    @PostMapping("/api/registro")
    RespuestaAutenticacionDTO altaMedico(@Valid @RequestBody RegistroDTO peticion){
        Usuario usuarioARegistrar = servicioAutenticacion.registro(peticion);

        servicioMedico.anadirMedico(usuarioARegistrar);

        usuarioARegistrar.setContrasena(peticion.contrasena());
        LoginDTO nuevoLogin = new LoginDTO(usuarioARegistrar.getEmail(), usuarioARegistrar.getContrasena());

        return servicioAutenticacion.login(nuevoLogin);
    }
}