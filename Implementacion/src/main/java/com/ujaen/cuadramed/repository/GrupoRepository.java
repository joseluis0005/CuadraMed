package com.ujaen.cuadramed.repository;

import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo,Long> {
    boolean existsByEspecialidad(Especialidad especialidad);
    boolean existsByMiembrosId(Long idMedico);

    Optional<Grupo> findByResponsableId(Long idUsuario);
    boolean existsByResponsableId(Long idUsuario);
}
