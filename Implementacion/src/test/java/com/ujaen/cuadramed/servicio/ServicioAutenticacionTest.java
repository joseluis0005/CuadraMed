package com.ujaen.cuadramed.servicio;

import com.ujaen.cuadramed.dto.LoginDTO;
import com.ujaen.cuadramed.dto.RespuestaAutenticacionDTO;
import com.ujaen.cuadramed.excepciones.CredencialesInvalidasExcep;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import com.ujaen.cuadramed.seguridad.JwtUtils;
import com.ujaen.cuadramed.service.ServicioAutenticacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioAutenticacionTest {

    @Mock
    private UsuarioRepository repositorioUsuario;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private ServicioAutenticacion servicioAutenticacion;

    @Test
    void loginExitosoDebeDevolverRespuestaDTO() {
        LoginDTO loginDTO = new LoginDTO("admin@cuadramed.es", "admin123");
        Usuario usuario = new Usuario("Admin", "663465813", "admin@cuadramed.es", "encodedPassword", Set.of(Rol.ADMINISTRADOR));

        when(repositorioUsuario.findByEmail("admin@cuadramed.es")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin123", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateToken(eq(usuario.getId()), eq(usuario.getRoles()), eq("admin@cuadramed.es")))
                .thenReturn("mockToken123");

        RespuestaAutenticacionDTO respuesta = servicioAutenticacion.login(loginDTO);

        assertNotNull(respuesta);
        assertEquals(Set.of(Rol.ADMINISTRADOR), respuesta.rol());
        assertEquals("mockToken123", respuesta.token());
    }

    @Test
    void loginConEmailInexistenteDebeLanzarExcepcion() {
        LoginDTO loginDTO = new LoginDTO("noexiste@cuadramed.es", "admin123");
        when(repositorioUsuario.findByEmail("noexiste@cuadramed.es")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasExcep.class, () -> servicioAutenticacion.login(loginDTO));
    }

    @Test
    void loginConPasswordInvalidaDebeLanzarExcepcion() {
        LoginDTO loginDTO = new LoginDTO("admin@cuadramed.es", "wrongpass");
        Usuario usuario = new Usuario("Admin", "663465813", "admin@cuadramed.es", "encodedPassword", Set.of(Rol.ADMINISTRADOR));

        when(repositorioUsuario.findByEmail("admin@cuadramed.es")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        assertThrows(CredencialesInvalidasExcep.class, () -> servicioAutenticacion.login(loginDTO));
    }

    @Test
    void loginConUsuarioInactivoDebeLanzarExcepcion() {
        LoginDTO loginDTO = new LoginDTO("admin@cuadramed.es", "admin123");
        Usuario usuario = new Usuario("Admin", "663465813", "admin@cuadramed.es", "encodedPassword", Set.of(Rol.ADMINISTRADOR));
        usuario.setActivo(false);

        when(repositorioUsuario.findByEmail("admin@cuadramed.es")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin123", "encodedPassword")).thenReturn(true);

        assertThrows(CredencialesInvalidasExcep.class, () -> servicioAutenticacion.login(loginDTO));
        verify(jwtUtils, never()).generateToken(any(), any(), any());
    }
}