package com.ujaen.cuadramed.controladores;

import com.ujaen.cuadramed.dto.AltaGrupoDTO;
import com.ujaen.cuadramed.dto.GrupoDTO;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.service.ServicioGrupo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grupo")
public class ControladorGrupo {
    @Autowired
    ServicioGrupo servicioGrupo;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/alta")
    public ResponseEntity<GrupoDTO> altaGrupo(@Valid @RequestBody AltaGrupoDTO peticion){
        Grupo grupo = servicioGrupo.anadirGrupo(new Grupo(peticion.nombre(), peticion.especialidad()));
        return ResponseEntity.ok(GrupoDTO.desde(grupo));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> bajaGrupo(@PathVariable Long id){
        servicioGrupo.eliminarGrupo(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','PLANIFICADOR')")
    @GetMapping("/{id}/consulta")
    public ResponseEntity<GrupoDTO> consultaGrupo(@PathVariable Long id){
        return ResponseEntity.ok(GrupoDTO.desde(servicioGrupo.buscarGrupo(id)));
    }

    @PreAuthorize("hasRole('PLANIFICADOR')")
    @PostMapping("/{id}/miembros/{idMedico}")
    public ResponseEntity<GrupoDTO> anadirMiembro(@PathVariable Long id, @PathVariable Long idMedico){
        return ResponseEntity.ok(GrupoDTO.desde(servicioGrupo.anadirMiembro(id, idMedico)));
    }

    @PreAuthorize("hasRole('PLANIFICADOR')")
    @DeleteMapping("/{id}/miembros/{idMedico}")
    public ResponseEntity<GrupoDTO> quitarMiembro(@PathVariable Long id, @PathVariable Long idMedico){
        return ResponseEntity.ok(GrupoDTO.desde(servicioGrupo.quitarMiembro(id, idMedico)));
    }
}
