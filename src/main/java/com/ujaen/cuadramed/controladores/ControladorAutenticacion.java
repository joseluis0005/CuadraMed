package com.ujaen.cuadramed.controladores;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.service.ServicioAutenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacion {

    @Autowired
    ServicioAutenticacion servicioAutenticacion;

    @PostMapping("/login")
    public ResponseEntity<RespuestaAutenticacionDTO> login(LoginDTO peticion){
        RespuestaAutenticacionDTO respuesta = servicioAutenticacion.login(peticion);

        return ResponseEntity.ok(respuesta);
    }
}
