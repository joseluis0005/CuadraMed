package com.ujaen.cuadramed.repository;

import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByActivoTrue();
    boolean existsByEmailAndId(String email, Long id);

    List<Usuario> findByRolesContaining(Rol rol);
}
