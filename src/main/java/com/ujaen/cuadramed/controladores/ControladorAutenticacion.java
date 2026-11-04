package com.ujaen.cuadramed.controladores;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.service.ServicioAutenticacion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorAutenticacion {

    @Autowired
    ServicioAutenticacion servicioAutenticacion;

    @PostMapping("/login")
    public ResponseEntity<RespuestaAutenticacionDTO> login(@Valid @RequestBody LoginDTO peticion){
        RespuestaAutenticacionDTO respuesta = servicioAutenticacion.login(peticion);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(){
        return ResponseEntity.ok("pong");
    }
}
