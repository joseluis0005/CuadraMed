package com.ujaen.cuadramed.controladores;

import com.ujaen.cuadramed.model.Especialidad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/especialidad")
public class ControladorEspecialidad {

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','PLANIFICADOR')")
    @GetMapping
    public List<Especialidad> listarEspecialidades(){
        return List.of(Especialidad.values());
    }
}
