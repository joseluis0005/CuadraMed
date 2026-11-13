package com.ujaen.cuadramed.repository;

import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo,Long> {
    boolean existsByEspecialidad(Especialidad especialidad);
    boolean existsByMiembrosId(Long idMedico);
}
