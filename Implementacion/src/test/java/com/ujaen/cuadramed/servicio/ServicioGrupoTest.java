package com.ujaen.cuadramed.servicio;

import com.ujaen.cuadramed.excepciones.*;
import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.GrupoRepository;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import com.ujaen.cuadramed.service.ServicioGrupo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de grupos (HU-04). Los repositorios se sustituyen por mocks
 * para comprobar solo la lógica del servicio: validaciones, orden de comprobaciones y llamadas a la BD.
 */
@ExtendWith(MockitoExtension.class)
class ServicioGrupoTest {

    private static final Long ID_GRUPO = 1L;
    private static final Long ID_PLANIFICADOR = 2L;
    private static final Long ID_MEDICO = 3L;
    private static final Long ID_OTRO_PLANIFICADOR = 4L;
    private static final String EMAIL_RESPONSABLE = "ana@cuadramed.es";
    private static final String EMAIL_OTRO_PLANIFICADOR = "laura@cuadramed.es";

    @Mock
    private GrupoRepository repositorioGrupo;

    @Mock
    private UsuarioRepository repositorioUsuario;

    @InjectMocks
    private ServicioGrupo servicioGrupo;

    private Usuario planificador;
    private Usuario otroPlanificador;
    private Usuario medico;

    @BeforeEach
    void setUp() {
        planificador = usuario("Ana", "ana@cuadramed.es", ID_PLANIFICADOR, Rol.PLANIFICADOR, Rol.MEDICO);
        otroPlanificador = usuario("Laura", "laura@cuadramed.es", ID_OTRO_PLANIFICADOR, Rol.PLANIFICADOR, Rol.MEDICO);
        medico = usuario("Marcos", "marcos@cuadramed.es", ID_MEDICO, Rol.MEDICO);
    }

    // El id lo genera la BD y no tiene setter, así que se fija por reflexión
    private Usuario usuario(String nombre, String email, Long id, Rol... roles) {
        Usuario u = new Usuario(nombre, "600000000", email, "pass", Set.of(roles));
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private Grupo grupoConResponsable(Usuario responsable) {
        Grupo g = new Grupo("Urgencias", Especialidad.URGENCIAS);
        ReflectionTestUtils.setField(g, "id", ID_GRUPO);
        g.asignarResponsable(responsable);
        return g;
    }

    // ==================== crearGrupo ====================

    @Test
    void crearGrupoGuardaElGrupoConSuResponsableComoMiembro() {
        when(repositorioGrupo.existsByEspecialidad(Especialidad.URGENCIAS)).thenReturn(false);
        when(repositorioUsuario.findById(ID_PLANIFICADOR)).thenReturn(Optional.of(planificador));
        when(repositorioGrupo.existsByResponsableId(ID_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.save(any(Grupo.class))).thenAnswer(inv -> inv.getArgument(0));

        Grupo grupo = servicioGrupo.crearGrupo("Urgencias", Especialidad.URGENCIAS, ID_PLANIFICADOR);

        assertEquals("Urgencias", grupo.getNombre());
        assertEquals(Especialidad.URGENCIAS, grupo.getEspecialidad());
        assertEquals(planificador, grupo.getResponsable());
        assertEquals(List.of(planificador), grupo.getMiembros());
        verify(repositorioGrupo, times(1)).save(grupo);
    }

    @Test
    void crearGrupoDevuelveElGrupoQueDevuelveElRepositorio() {
        Grupo guardado = new Grupo("Urgencias", Especialidad.URGENCIAS);
        ReflectionTestUtils.setField(guardado, "id", 10L);
        when(repositorioGrupo.existsByEspecialidad(Especialidad.URGENCIAS)).thenReturn(false);
        when(repositorioUsuario.findById(ID_PLANIFICADOR)).thenReturn(Optional.of(planificador));
        when(repositorioGrupo.existsByResponsableId(ID_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.save(any(Grupo.class))).thenReturn(guardado);

        Grupo resultado = servicioGrupo.crearGrupo("Urgencias", Especialidad.URGENCIAS, ID_PLANIFICADOR);

        assertSame(guardado, resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    void crearGrupoConEspecialidadYaExistenteLanzaExcepcionSinConsultarElUsuario() {
        when(repositorioGrupo.existsByEspecialidad(Especialidad.URGENCIAS)).thenReturn(true);

        assertThrows(GrupoYaExistenteExcep.class,
                () -> servicioGrupo.crearGrupo("Urgencias", Especialidad.URGENCIAS, ID_PLANIFICADOR));

        verify(repositorioUsuario, never()).findById(anyLong());
        verify(repositorioGrupo, never()).save(any());
    }

    @Test
    void crearGrupoConResponsableInexistenteLanzaExcepcion() {
        when(repositorioGrupo.existsByEspecialidad(Especialidad.URGENCIAS)).thenReturn(false);
        when(repositorioUsuario.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MedicoNoEncontradoExcep.class,
                () -> servicioGrupo.crearGrupo("Urgencias", Especialidad.URGENCIAS, 99L));
        verify(repositorioGrupo, never()).save(any());
    }

    @Test
    void crearGrupoConResponsableQueYaDirigeOtroGrupoLanzaExcepcion() {
        when(repositorioGrupo.existsByEspecialidad(Especialidad.PEDIATRIA)).thenReturn(false);
        when(repositorioUsuario.findById(ID_PLANIFICADOR)).thenReturn(Optional.of(planificador));
        when(repositorioGrupo.existsByResponsableId(ID_PLANIFICADOR)).thenReturn(true);

        assertThrows(ResponsableYaAsignadoExcep.class,
                () -> servicioGrupo.crearGrupo("Pediatría", Especialidad.PEDIATRIA, ID_PLANIFICADOR));
        verify(repositorioGrupo, never()).save(any());
    }

    @Test
    void crearGrupoConResponsableQueEsMiembroDeOtroGrupoLanzaExcepcion() {
        when(repositorioGrupo.existsByEspecialidad(Especialidad.PEDIATRIA)).thenReturn(false);
        when(repositorioUsuario.findById(ID_PLANIFICADOR)).thenReturn(Optional.of(planificador));
        when(repositorioGrupo.existsByResponsableId(ID_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_PLANIFICADOR)).thenReturn(true);

        assertThrows(MedicoYaEnGrupoExcep.class,
                () -> servicioGrupo.crearGrupo("Pediatría", Especialidad.PEDIATRIA, ID_PLANIFICADOR));
        verify(repositorioGrupo, never()).save(any());
    }

    @Test
    void crearGrupoConResponsableSinRolPlanificadorLanzaExcepcion() {
        when(repositorioGrupo.existsByEspecialidad(Especialidad.URGENCIAS)).thenReturn(false);
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(repositorioGrupo.existsByResponsableId(ID_MEDICO)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_MEDICO)).thenReturn(false);

        assertThrows(UsuarioNoEsPlanificadorExcep.class,
                () -> servicioGrupo.crearGrupo("Urgencias", Especialidad.URGENCIAS, ID_MEDICO));
        verify(repositorioGrupo, never()).save(any());
    }

    // ==================== eliminarGrupo ====================

    @Test
    void eliminarGrupoExistenteLoBorra() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        servicioGrupo.eliminarGrupo(ID_GRUPO);

        verify(repositorioGrupo, times(1)).delete(grupo);
    }

    @Test
    void eliminarGrupoInexistenteLanzaExcepcionYNoBorraNada() {
        when(repositorioGrupo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GrupoNoEncontradoExcep.class, () -> servicioGrupo.eliminarGrupo(99L));
        verify(repositorioGrupo, never()).delete(any());
    }

    // ==================== buscarGrupo y getGrupos ====================

    @Test
    void buscarGrupoExistenteLoDevuelve() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        assertSame(grupo, servicioGrupo.buscarGrupo(ID_GRUPO));
    }

    @Test
    void buscarGrupoInexistenteLanzaExcepcion() {
        when(repositorioGrupo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GrupoNoEncontradoExcep.class, () -> servicioGrupo.buscarGrupo(99L));
    }

    @Test
    void getGruposDevuelveTodosLosGrupos() {
        Grupo urgencias = grupoConResponsable(planificador);
        Grupo pediatria = new Grupo("Pediatría", Especialidad.PEDIATRIA);
        pediatria.asignarResponsable(otroPlanificador);
        when(repositorioGrupo.findAll()).thenReturn(List.of(urgencias, pediatria));

        List<Grupo> grupos = servicioGrupo.getGrupos();

        assertEquals(2, grupos.size());
        assertTrue(grupos.containsAll(List.of(urgencias, pediatria)));
    }

    @Test
    void getGruposSinGruposDevuelveListaVacia() {
        when(repositorioGrupo.findAll()).thenReturn(List.of());

        assertTrue(servicioGrupo.getGrupos().isEmpty());
    }

    // ==================== anadirMiembro ====================

    @Test
    void anadirMiembroIncluyeAlMedicoYDevuelveElGrupo() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(repositorioGrupo.existsByMiembrosId(ID_MEDICO)).thenReturn(false);

        Grupo resultado = servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE);

        assertSame(grupo, resultado);
        assertTrue(resultado.getMiembros().contains(medico));
        assertEquals(2, resultado.getMiembros().size());
    }

    @Test
    void anadirMiembroAGrupoInexistenteLanzaExcepcion() {
        when(repositorioGrupo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GrupoNoEncontradoExcep.class, () -> servicioGrupo.anadirMiembro(99L, ID_MEDICO, EMAIL_RESPONSABLE));
        verify(repositorioUsuario, never()).findById(anyLong());
    }

    @Test
    void anadirMiembroInexistenteLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MedicoNoEncontradoExcep.class, () -> servicioGrupo.anadirMiembro(ID_GRUPO, 99L, EMAIL_RESPONSABLE));
        assertEquals(1, grupo.getMiembros().size());
    }

    @Test
    void anadirMedicoQueYaPerteneceAOtroGrupoLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(repositorioGrupo.existsByMiembrosId(ID_MEDICO)).thenReturn(true);

        assertThrows(MedicoYaEnGrupoExcep.class, () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE));
        assertFalse(grupo.getMiembros().contains(medico));
    }

    @Test
    void anadirMedicoQueYaEstaEnEsteGrupoLanzaExcepcionSinConsultarOtrosGrupos() {
        Grupo grupo = grupoConResponsable(planificador);
        grupo.anadirMiembro(medico);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));

        assertThrows(MedicoYaEnGrupoExcep.class, () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE));
        verify(repositorioGrupo, never()).existsByMiembrosId(anyLong());
        assertEquals(2, grupo.getMiembros().size());
    }

    @Test
    void anadirMiembroDadoDeBajaLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        medico.setActivo(false);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(repositorioGrupo.existsByMiembrosId(ID_MEDICO)).thenReturn(false);

        assertThrows(MedicoNoActivoExcep.class, () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE));
    }

    // ==================== quitarMiembro ====================

    @Test
    void quitarMiembroLoEliminaYDevuelveElGrupo() {
        Grupo grupo = grupoConResponsable(planificador);
        grupo.anadirMiembro(medico);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));

        Grupo resultado = servicioGrupo.quitarMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE);

        assertSame(grupo, resultado);
        assertFalse(resultado.getMiembros().contains(medico));
    }

    @Test
    void quitarMiembroDeGrupoInexistenteLanzaExcepcion() {
        when(repositorioGrupo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GrupoNoEncontradoExcep.class, () -> servicioGrupo.quitarMiembro(99L, ID_MEDICO, EMAIL_RESPONSABLE));
    }

    @Test
    void quitarMiembroInexistenteLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MedicoNoEncontradoExcep.class, () -> servicioGrupo.quitarMiembro(ID_GRUPO, 99L, EMAIL_RESPONSABLE));
    }

    @Test
    void quitarMedicoQueNoEstaEnElGrupoLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));

        assertThrows(MedicoNoEnGrupoExcep.class, () -> servicioGrupo.quitarMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE));
    }

    @Test
    void quitarAlResponsableLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_PLANIFICADOR)).thenReturn(Optional.of(planificador));

        assertThrows(ExpulsionDePlanificadorExcep.class, () -> servicioGrupo.quitarMiembro(ID_GRUPO, ID_PLANIFICADOR, EMAIL_RESPONSABLE));
        assertTrue(grupo.getMiembros().contains(planificador));
    }

    // ==================== Solo el responsable gestiona su grupo ====================

    @Test
    void anadirMiembroSiNoEresElResponsableLanzaAccesoDenegado() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        assertThrows(AccesoGrupoDenegadoExcep.class,
                () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_OTRO_PLANIFICADOR));
        assertEquals(1, grupo.getMiembros().size());
    }

    @Test
    void anadirMiembroSinPermisoNoConsultaNadaMas() {
        // El permiso va primero: no se revela si el médico existe o si está en otro grupo
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        assertThrows(AccesoGrupoDenegadoExcep.class,
                () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_OTRO_PLANIFICADOR));
        verify(repositorioUsuario, never()).findById(anyLong());
        verify(repositorioGrupo, never()).existsByMiembrosId(anyLong());
    }

    @Test
    void quitarMiembroSiNoEresElResponsableLanzaAccesoDenegado() {
        Grupo grupo = grupoConResponsable(planificador);
        grupo.anadirMiembro(medico);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        assertThrows(AccesoGrupoDenegadoExcep.class,
                () -> servicioGrupo.quitarMiembro(ID_GRUPO, ID_MEDICO, EMAIL_OTRO_PLANIFICADOR));
        assertTrue(grupo.getMiembros().contains(medico));
        verify(repositorioUsuario, never()).findById(anyLong());
    }

    @Test
    void elAntiguoResponsablePierdeElPermisoTrasElCambio() {
        Grupo grupo = grupoConResponsable(planificador);
        grupo.asignarResponsable(otroPlanificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        assertThrows(AccesoGrupoDenegadoExcep.class,
                () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_RESPONSABLE));
    }

    @Test
    void elNuevoResponsablePuedeGestionarElGrupoTrasElCambio() {
        Grupo grupo = grupoConResponsable(planificador);
        grupo.asignarResponsable(otroPlanificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(repositorioGrupo.existsByMiembrosId(ID_MEDICO)).thenReturn(false);

        Grupo resultado = servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, EMAIL_OTRO_PLANIFICADOR);

        assertTrue(resultado.getMiembros().contains(medico));
    }

    @Test
    void sinUsuarioAutenticadoNoSePuedeGestionarElGrupo() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));

        assertThrows(AccesoGrupoDenegadoExcep.class,
                () -> servicioGrupo.anadirMiembro(ID_GRUPO, ID_MEDICO, null));
    }

    // ==================== cambiarPlanificador ====================

    @Test
    void cambiarPlanificadorAsignaAlNuevoYMantieneAlAnteriorComoMiembro() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_OTRO_PLANIFICADOR)).thenReturn(Optional.of(otroPlanificador));
        when(repositorioGrupo.existsByResponsableId(ID_OTRO_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_OTRO_PLANIFICADOR)).thenReturn(false);

        Grupo resultado = servicioGrupo.cambiarPlanificador(ID_GRUPO, ID_OTRO_PLANIFICADOR);

        assertSame(grupo, resultado);
        assertEquals(otroPlanificador, resultado.getResponsable());
        assertTrue(resultado.getMiembros().containsAll(List.of(planificador, otroPlanificador)));
        // No hace falta save(): la entidad está gestionada dentro de la transacción
        verify(repositorioGrupo, never()).save(any());
    }

    @Test
    void cambiarPlanificadorPorElMismoResponsableNoHaceNada() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_PLANIFICADOR)).thenReturn(Optional.of(planificador));

        Grupo resultado = servicioGrupo.cambiarPlanificador(ID_GRUPO, ID_PLANIFICADOR);

        assertEquals(planificador, resultado.getResponsable());
        assertEquals(1, resultado.getMiembros().size());
        // Si no se corta antes, existsByResponsableId daría true y lanzaría ResponsableYaAsignadoExcep
        verify(repositorioGrupo, never()).existsByResponsableId(anyLong());
    }

    @Test
    void cambiarPlanificadorAUnMiembroDelPropioGrupoNoLoDuplica() {
        Grupo grupo = grupoConResponsable(planificador);
        grupo.anadirMiembro(otroPlanificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_OTRO_PLANIFICADOR)).thenReturn(Optional.of(otroPlanificador));
        when(repositorioGrupo.existsByResponsableId(ID_OTRO_PLANIFICADOR)).thenReturn(false);

        Grupo resultado = servicioGrupo.cambiarPlanificador(ID_GRUPO, ID_OTRO_PLANIFICADOR);

        assertEquals(otroPlanificador, resultado.getResponsable());
        assertEquals(2, resultado.getMiembros().size());
        // Ya es miembro de este grupo, así que no se comprueba si está en otro
        verify(repositorioGrupo, never()).existsByMiembrosId(anyLong());
    }

    @Test
    void cambiarPlanificadorAUsuarioQueYaDirigeOtroGrupoLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_OTRO_PLANIFICADOR)).thenReturn(Optional.of(otroPlanificador));
        when(repositorioGrupo.existsByResponsableId(ID_OTRO_PLANIFICADOR)).thenReturn(true);

        assertThrows(ResponsableYaAsignadoExcep.class,
                () -> servicioGrupo.cambiarPlanificador(ID_GRUPO, ID_OTRO_PLANIFICADOR));
        assertEquals(planificador, grupo.getResponsable());
        assertFalse(grupo.getMiembros().contains(otroPlanificador));
    }

    @Test
    void cambiarPlanificadorAMiembroDeOtroGrupoLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_OTRO_PLANIFICADOR)).thenReturn(Optional.of(otroPlanificador));
        when(repositorioGrupo.existsByResponsableId(ID_OTRO_PLANIFICADOR)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_OTRO_PLANIFICADOR)).thenReturn(true);

        assertThrows(MedicoYaEnGrupoExcep.class,
                () -> servicioGrupo.cambiarPlanificador(ID_GRUPO, ID_OTRO_PLANIFICADOR));
        assertEquals(planificador, grupo.getResponsable());
    }

    @Test
    void cambiarPlanificadorAUsuarioSinRolPlanificadorLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(repositorioGrupo.existsByResponsableId(ID_MEDICO)).thenReturn(false);
        when(repositorioGrupo.existsByMiembrosId(ID_MEDICO)).thenReturn(false);

        assertThrows(UsuarioNoEsPlanificadorExcep.class,
                () -> servicioGrupo.cambiarPlanificador(ID_GRUPO, ID_MEDICO));
        assertEquals(planificador, grupo.getResponsable());
        assertFalse(grupo.getMiembros().contains(medico));
    }

    @Test
    void cambiarPlanificadorDeGrupoInexistenteLanzaExcepcion() {
        when(repositorioGrupo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GrupoNoEncontradoExcep.class,
                () -> servicioGrupo.cambiarPlanificador(99L, ID_OTRO_PLANIFICADOR));
    }

    @Test
    void cambiarPlanificadorAUsuarioInexistenteLanzaExcepcion() {
        Grupo grupo = grupoConResponsable(planificador);
        when(repositorioGrupo.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(repositorioUsuario.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MedicoNoEncontradoExcep.class,
                () -> servicioGrupo.cambiarPlanificador(ID_GRUPO, 99L));
        assertEquals(planificador, grupo.getResponsable());
    }
}
