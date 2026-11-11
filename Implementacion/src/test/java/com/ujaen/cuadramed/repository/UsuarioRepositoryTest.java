package com.ujaen.cuadramed.repository;

import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void debeGuardarYBuscarUsuarioPorEmail() {
        Usuario usuario = new Usuario("Test User", "600000000", "test@cuadramed.es", "pass123", new HashSet<>(Arrays.asList(Rol.MEDICO)));
        usuarioRepository.save(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("test@cuadramed.es");

        assertTrue(encontrado.isPresent());
        assertEquals("Test User", encontrado.get().getNombre());
        assertTrue(encontrado.get().getRoles().contains(Rol.MEDICO));
    }

    @Test
    void debeVerificarSiExisteEmail() {
        Usuario usuario = new Usuario("Test User", "600000000", "existe@cuadramed.es", "pass123", new HashSet<>(Arrays.asList(Rol.MEDICO)));
        usuarioRepository.save(usuario);

        assertTrue(usuarioRepository.existsByEmail("existe@cuadramed.es"));
        assertFalse(usuarioRepository.existsByEmail("noexiste@cuadramed.es"));
    }

    @Test
    void debeFiltrarSoloMedicosActivos() {
        Usuario medicoActivo = new Usuario("Médico Activo", "600111222", "activo@cuadramed.es", "pass", new HashSet<>(Arrays.asList(Rol.MEDICO)));
        Usuario medicoInactivo = new Usuario("Médico Inactivo", "600333444", "inactivo@cuadramed.es", "pass", new HashSet<>(Arrays.asList(Rol.MEDICO)));
        medicoInactivo.setActivo(false);

        usuarioRepository.save(medicoActivo);
        usuarioRepository.save(medicoInactivo);

        List<Usuario> activos = usuarioRepository.findByActivoTrue();

        assertTrue(activos.stream().anyMatch(u -> u.getEmail().equals("activo@cuadramed.es")));
        assertFalse(activos.stream().anyMatch(u -> u.getEmail().equals("inactivo@cuadramed.es")));
    }

    @Test
    void debeVerificarSiExisteEmailEnOtroId() {
        Usuario usuario1 = usuarioRepository.save(new Usuario("User 1", "600111222", "u1@cuadramed.es", "pass", new HashSet<>(Arrays.asList(Rol.MEDICO))));

        assertTrue(usuarioRepository.existsByEmailAndId("u1@cuadramed.es", usuario1.getId()));
        assertFalse(usuarioRepository.existsByEmailAndId("u1@cuadramed.es", 999L));
    }
}