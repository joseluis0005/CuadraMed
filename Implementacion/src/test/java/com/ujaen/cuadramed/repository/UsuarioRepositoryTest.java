package com.ujaen.cuadramed.repository;

import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void debeGuardarYBuscarUsuarioPorEmail() {
        Usuario usuario = new Usuario("Test User", "600000000", "test@cuadramed.es", "pass123", Rol.MEDICO);
        usuarioRepository.save(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("test@cuadramed.es");

        assertTrue(encontrado.isPresent());
        assertEquals("Test User", encontrado.get().getNombre());
        assertEquals(Rol.MEDICO, encontrado.get().getRol());
    }

    @Test
    void debeVerificarSiExisteEmail() {
        Usuario usuario = new Usuario("Test User", "600000000", "existe@cuadramed.es", "pass123", Rol.MEDICO);
        usuarioRepository.save(usuario);

        assertTrue(usuarioRepository.existsByEmail("existe@cuadramed.es"));
        assertFalse(usuarioRepository.existsByEmail("noexiste@cuadramed.es"));
    }
}