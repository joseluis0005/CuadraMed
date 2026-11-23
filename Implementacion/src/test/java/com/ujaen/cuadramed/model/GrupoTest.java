package com.ujaen.cuadramed.model;

import com.ujaen.cuadramed.excepciones.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de las reglas de negocio de la entidad Grupo (HU-04).
 * No necesitan Spring ni base de datos: se prueba el objeto de dominio directamente.
 */
class GrupoTest {

    private Grupo grupo;
    private Usuario planificador;
    private Usuario otroPlanificador;
    private Usuario medico;
    private Usuario otroMedico;

    @BeforeEach
    void setUp() {
        grupo = new Grupo("Urgencias", Especialidad.URGENCIAS);
        planificador = new Usuario("Ana", "600000001", "ana@cuadramed.es", "pass", Set.of(Rol.PLANIFICADOR, Rol.MEDICO));
        otroPlanificador = new Usuario("Laura", "600000002", "laura@cuadramed.es", "pass", Set.of(Rol.PLANIFICADOR, Rol.MEDICO));
        medico = new Usuario("Marcos", "600000003", "marcos@cuadramed.es", "pass", Set.of(Rol.MEDICO));
        otroMedico = new Usuario("Pedro", "600000004", "pedro@cuadramed.es", "pass", Set.of(Rol.MEDICO));
    }

    // ---------- Creación ----------

    @Test
    void grupoNuevoTieneNombreEspecialidadYNingunMiembro() {
        assertEquals("Urgencias", grupo.getNombre());
        assertEquals(Especialidad.URGENCIAS, grupo.getEspecialidad());
        assertTrue(grupo.getMiembros().isEmpty());
        assertNull(grupo.getResponsable());
        assertNull(grupo.getId());
    }

    // ---------- Añadir miembros ----------

    @Test
    void anadirMedicoActivoLoIncluyeEnElGrupo() {
        grupo.anadirMiembro(medico);

        assertEquals(1, grupo.getMiembros().size());
        assertTrue(grupo.getMiembros().contains(medico));
    }

    @Test
    void sePuedenAnadirVariosMedicos() {
        grupo.anadirMiembro(medico);
        grupo.anadirMiembro(otroMedico);

        assertEquals(2, grupo.getMiembros().size());
        assertTrue(grupo.getMiembros().containsAll(Set.of(medico, otroMedico)));
    }

    @Test
    void anadirUsuarioQueNoEsMedicoLanzaExcepcion() {
        Usuario admin = new Usuario("Admin", "600000005", "admin@cuadramed.es", "pass", Set.of(Rol.ADMINISTRADOR));

        assertThrows(UsuarioNoEsMedicoExcep.class, () -> grupo.anadirMiembro(admin));
        assertTrue(grupo.getMiembros().isEmpty());
    }

    @Test
    void anadirMedicoDadoDeBajaLanzaExcepcion() {
        medico.setActivo(false);

        assertThrows(MedicoNoActivoExcep.class, () -> grupo.anadirMiembro(medico));
        assertTrue(grupo.getMiembros().isEmpty());
    }

    @Test
    void anadirDosVecesElMismoMedicoLanzaExcepcion() {
        grupo.anadirMiembro(medico);

        assertThrows(MedicoYaEnGrupoExcep.class, () -> grupo.anadirMiembro(medico));
        assertEquals(1, grupo.getMiembros().size());
    }

    // ---------- Quitar miembros ----------

    @Test
    void quitarMiembroLoEliminaDelGrupo() {
        grupo.anadirMiembro(medico);
        grupo.anadirMiembro(otroMedico);

        grupo.quitarMiembro(medico);

        assertFalse(grupo.getMiembros().contains(medico));
        assertTrue(grupo.getMiembros().contains(otroMedico));
    }

    @Test
    void quitarMedicoQueNoPerteneceAlGrupoLanzaExcepcion() {
        grupo.anadirMiembro(otroMedico);

        assertThrows(MedicoNoEnGrupoExcep.class, () -> grupo.quitarMiembro(medico));
        assertEquals(1, grupo.getMiembros().size());
    }

    @Test
    void noSePuedeQuitarAlResponsableDelGrupo() {
        grupo.asignarResponsable(planificador);

        assertThrows(ExpulsionDePlanificadorExcep.class, () -> grupo.quitarMiembro(planificador));
        assertTrue(grupo.getMiembros().contains(planificador));
        assertEquals(planificador, grupo.getResponsable());
    }

    @Test
    void sePuedeVolverAAnadirUnMedicoQueSeHabiaQuitado() {
        grupo.anadirMiembro(medico);
        grupo.quitarMiembro(medico);

        grupo.anadirMiembro(medico);

        assertTrue(grupo.getMiembros().contains(medico));
    }

    // ---------- Responsable ----------

    @Test
    void asignarResponsableLoFijaYLoAnadeComoMiembro() {
        grupo.asignarResponsable(planificador);

        assertEquals(planificador, grupo.getResponsable());
        assertTrue(grupo.getMiembros().contains(planificador));
    }

    @Test
    void asignarResponsableQueYaEsMiembroNoLoDuplica() {
        grupo.anadirMiembro(planificador);

        grupo.asignarResponsable(planificador);

        assertEquals(planificador, grupo.getResponsable());
        assertEquals(1, grupo.getMiembros().size());
    }

    @Test
    void asignarResponsableSinRolPlanificadorLanzaExcepcion() {
        assertThrows(UsuarioNoEsPlanificadorExcep.class, () -> grupo.asignarResponsable(medico));
        assertNull(grupo.getResponsable());
        assertTrue(grupo.getMiembros().isEmpty());
    }

    @Test
    void asignarResponsablePlanificadorQueNoEsMedicoLanzaExcepcion() {
        // El responsable es un médico más del grupo, así que también necesita el rol MEDICO
        Usuario soloPlanificador = new Usuario("Solo Plan", "600000006", "plan@cuadramed.es", "pass", Set.of(Rol.PLANIFICADOR));

        assertThrows(UsuarioNoEsMedicoExcep.class, () -> grupo.asignarResponsable(soloPlanificador));
        assertNull(grupo.getResponsable());
        assertTrue(grupo.getMiembros().isEmpty());
    }

    @Test
    void asignarResponsableDadoDeBajaLanzaExcepcion() {
        planificador.setActivo(false);

        assertThrows(MedicoNoActivoExcep.class, () -> grupo.asignarResponsable(planificador));
        assertNull(grupo.getResponsable());
    }

    @Test
    void cambiarResponsableMantieneAlAnteriorComoMiembro() {
        grupo.asignarResponsable(planificador);

        grupo.asignarResponsable(otroPlanificador);

        assertEquals(otroPlanificador, grupo.getResponsable());
        assertTrue(grupo.getMiembros().containsAll(Set.of(planificador, otroPlanificador)));
        assertEquals(2, grupo.getMiembros().size());
    }

    @Test
    void trasCambiarResponsableSePuedeQuitarAlAnterior() {
        grupo.asignarResponsable(planificador);
        grupo.asignarResponsable(otroPlanificador);

        grupo.quitarMiembro(planificador);

        assertFalse(grupo.getMiembros().contains(planificador));
        assertEquals(otroPlanificador, grupo.getResponsable());
    }

    @Test
    void unCambioDeResponsableFallidoNoAlteraElGrupo() {
        grupo.asignarResponsable(planificador);

        assertThrows(UsuarioNoEsPlanificadorExcep.class, () -> grupo.asignarResponsable(medico));

        assertEquals(planificador, grupo.getResponsable());
        assertEquals(1, grupo.getMiembros().size());
    }
}
