package com.ujaen.cuadramed.servicio;

import com.ujaen.cuadramed.excepciones.EmailYaRegistradoExcep;
import com.ujaen.cuadramed.excepciones.MedicoNoEncontradoExcep;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import com.ujaen.cuadramed.service.ServicioMedico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioMedicoTest {

    @Mock
    private UsuarioRepository repositorioUsuario;

    @InjectMocks
    private ServicioMedico servicioMedico;

    private Usuario medicoPrueba;

    @BeforeEach
    void setUp() {
        medicoPrueba = new Usuario("Dr. García", "611223344", "garcia@cuadramed.es", "encodedPass", new HashSet<>(Arrays.asList(Rol.MEDICO)));
    }

    @Test
    void anadirMedicoDebePersistirEnRepositorio() {
        servicioMedico.anadirMedico(medicoPrueba);
        verify(repositorioUsuario, times(1)).save(medicoPrueba);
    }

    @Test
    void listarMedicosActivosDebeRetornarSoloMedicosActivos() {
        when(repositorioUsuario.findByActivoTrue()).thenReturn(List.of(medicoPrueba));

        List<Usuario> activos = servicioMedico.listarMedicosActivos();

        assertEquals(1, activos.size());
        assertTrue(activos.get(0).isActivo());
    }

    @Test
    void actualizarMedicoExitosamente() {
        Usuario datosModificados = new Usuario("Dr. García Modificado", "699999999", "garcia.nuevo@cuadramed.es", "encodedPass", new HashSet<>(Arrays.asList(Rol.MEDICO)));
        when(repositorioUsuario.findById(1L)).thenReturn(Optional.of(medicoPrueba));
        when(repositorioUsuario.existsByEmailAndId("garcia.nuevo@cuadramed.es", 1L)).thenReturn(false);

        Usuario actualizado = servicioMedico.actualizar(1L, datosModificados);

        assertEquals("Dr. García Modificado", actualizado.getNombre());
        assertEquals("garcia.nuevo@cuadramed.es", actualizado.getEmail());
        assertEquals("699999999", actualizado.getTelefono());
    }

    @Test
    void actualizarMedicoConEmailDuplicadoDebeLanzarExcepcion() {
        Usuario datosModificados = new Usuario("Dr. García", "611223344", "existe@cuadramed.es", "encodedPass", new HashSet<>(Arrays.asList(Rol.MEDICO)));
        when(repositorioUsuario.findById(1L)).thenReturn(Optional.of(medicoPrueba));
        when(repositorioUsuario.existsByEmailAndId("existe@cuadramed.es", 1L)).thenReturn(true);

        assertThrows(EmailYaRegistradoExcep.class, () -> servicioMedico.actualizar(1L, datosModificados));
    }

    @Test
    void darDeBajaCambiaEstadoActivoAFalse() {
        when(repositorioUsuario.findById(1L)).thenReturn(Optional.of(medicoPrueba));

        servicioMedico.darDeBaja(1L);

        assertFalse(medicoPrueba.isActivo());
    }

    @Test
    void darDeBajaMedicoInexistenteDebeLanzarExcepcion() {
        when(repositorioUsuario.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MedicoNoEncontradoExcep.class, () -> servicioMedico.darDeBaja(99L));
    }

    @Test
    void buscarMedicoConRolNoMedicoDebeLanzarExcepcion() {
        Usuario usuarioAdmin = new Usuario("Admin", "600000000", "admin@cuadramed.es", "pass", new HashSet<>(Arrays.asList(Rol.ADMINISTRADOR)));
        when(repositorioUsuario.findById(2L)).thenReturn(Optional.of(usuarioAdmin));

        assertThrows(MedicoNoEncontradoExcep.class, () -> servicioMedico.darDeBaja(2L));
    }
}