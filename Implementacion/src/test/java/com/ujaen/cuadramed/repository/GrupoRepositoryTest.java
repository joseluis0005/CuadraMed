package com.ujaen.cuadramed.repository;

import com.ujaen.cuadramed.model.Especialidad;
import com.ujaen.cuadramed.model.Grupo;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de persistencia de Grupo (HU-04) contra una base de datos H2 en memoria.
 * Se comprueban las consultas derivadas del repositorio y las restricciones del mapeo JPA
 * (responsable obligatorio y único, relación con los miembros).
 */
@DataJpaTest
class GrupoRepositoryTest {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    private Usuario planificador;
    private Usuario otroPlanificador;
    private Usuario medico;

    @BeforeEach
    void setUp() {
        planificador = usuarioRepository.save(new Usuario("Ana", "600000001", "ana@test.es", "pass", Set.of(Rol.PLANIFICADOR, Rol.MEDICO)));
        otroPlanificador = usuarioRepository.save(new Usuario("Laura", "600000002", "laura@test.es", "pass", Set.of(Rol.PLANIFICADOR, Rol.MEDICO)));
        medico = usuarioRepository.save(new Usuario("Marcos", "600000003", "marcos@test.es", "pass", Set.of(Rol.MEDICO)));
    }

    private Grupo guardarGrupo(String nombre, Especialidad especialidad, Usuario responsable) {
        Grupo grupo = new Grupo(nombre, especialidad);
        grupo.asignarResponsable(responsable);
        return grupoRepository.save(grupo);
    }

    // Vacía la caché de JPA para que las lecturas siguientes vayan de verdad a la base de datos
    private void sincronizarConBD() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void guardarGrupoPersisteResponsableYMiembros() {
        Grupo grupo = guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        grupo.anadirMiembro(medico);
        sincronizarConBD();

        Grupo recuperado = grupoRepository.findById(grupo.getId()).orElseThrow();

        assertEquals("Urgencias", recuperado.getNombre());
        assertEquals(Especialidad.URGENCIAS, recuperado.getEspecialidad());
        assertEquals(planificador.getId(), recuperado.getResponsable().getId());
        assertEquals(2, recuperado.getMiembros().size());
        assertTrue(recuperado.getMiembros().stream().anyMatch(u -> u.getId().equals(medico.getId())));
    }

    @Test
    void existsByEspecialidadIndicaSiYaHayGrupoDeEsaEspecialidad() {
        guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);

        assertTrue(grupoRepository.existsByEspecialidad(Especialidad.URGENCIAS));
        assertFalse(grupoRepository.existsByEspecialidad(Especialidad.PEDIATRIA));
    }

    @Test
    void existsByMiembrosIdIndicaSiElMedicoPerteneceAAlgunGrupo() {
        Grupo grupo = guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        grupo.anadirMiembro(medico);
        sincronizarConBD();

        assertTrue(grupoRepository.existsByMiembrosId(medico.getId()));
        assertTrue(grupoRepository.existsByMiembrosId(planificador.getId()));
        assertFalse(grupoRepository.existsByMiembrosId(otroPlanificador.getId()));
    }

    @Test
    void existsByResponsableIdIndicaSiElUsuarioDirigeAlgunGrupo() {
        guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        sincronizarConBD();

        assertTrue(grupoRepository.existsByResponsableId(planificador.getId()));
        assertFalse(grupoRepository.existsByResponsableId(otroPlanificador.getId()));
        assertFalse(grupoRepository.existsByResponsableId(medico.getId()));
    }

    @Test
    void findByResponsableIdDevuelveElGrupoQueDirige() {
        Grupo urgencias = guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        guardarGrupo("Pediatría", Especialidad.PEDIATRIA, otroPlanificador);
        sincronizarConBD();

        Optional<Grupo> encontrado = grupoRepository.findByResponsableId(planificador.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(urgencias.getId(), encontrado.get().getId());
    }

    @Test
    void findByResponsableIdDeUsuarioQueNoDirigeNingunGrupoDevuelveVacio() {
        guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        sincronizarConBD();

        assertTrue(grupoRepository.findByResponsableId(medico.getId()).isEmpty());
    }

    @Test
    void quitarMiembroSePersisteEnLaBD() {
        Grupo grupo = guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        grupo.anadirMiembro(medico);
        sincronizarConBD();

        Grupo recuperado = grupoRepository.findById(grupo.getId()).orElseThrow();
        Usuario medicoRecuperado = usuarioRepository.findById(medico.getId()).orElseThrow();
        recuperado.quitarMiembro(medicoRecuperado);
        sincronizarConBD();

        assertFalse(grupoRepository.existsByMiembrosId(medico.getId()));
        assertEquals(1, grupoRepository.findById(grupo.getId()).orElseThrow().getMiembros().size());
    }

    @Test
    void cambiarResponsableSePersisteEnLaBD() {
        Grupo grupo = guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        grupo.asignarResponsable(otroPlanificador);
        sincronizarConBD();

        Grupo recuperado = grupoRepository.findById(grupo.getId()).orElseThrow();

        assertEquals(otroPlanificador.getId(), recuperado.getResponsable().getId());
        assertFalse(grupoRepository.existsByResponsableId(planificador.getId()));
        assertTrue(grupoRepository.existsByMiembrosId(planificador.getId()));
    }

    @Test
    void eliminarGrupoNoBorraALosUsuariosYLosDejaSinGrupo() {
        Grupo grupo = guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        grupo.anadirMiembro(medico);
        sincronizarConBD();

        grupoRepository.delete(grupoRepository.findById(grupo.getId()).orElseThrow());
        sincronizarConBD();

        assertTrue(grupoRepository.findById(grupo.getId()).isEmpty());
        assertTrue(usuarioRepository.findById(medico.getId()).isPresent());
        assertTrue(usuarioRepository.findById(planificador.getId()).isPresent());
        assertFalse(grupoRepository.existsByMiembrosId(medico.getId()));
        assertFalse(grupoRepository.existsByResponsableId(planificador.getId()));
    }

    @Test
    void unUsuarioNoPuedeSerResponsableDeDosGruposEnLaBD() {
        // El servicio lo impide antes, pero la restricción UNIQUE es la última barrera
        guardarGrupo("Urgencias", Especialidad.URGENCIAS, planificador);
        sincronizarConBD();

        Grupo otro = new Grupo("Pediatría", Especialidad.PEDIATRIA);
        otro.asignarResponsable(usuarioRepository.findById(planificador.getId()).orElseThrow());

        assertThrows(DataIntegrityViolationException.class, () -> grupoRepository.saveAndFlush(otro));
    }

    @Test
    void noSePuedeGuardarUnGrupoSinResponsable() {
        Grupo sinResponsable = new Grupo("Urgencias", Especialidad.URGENCIAS);

        assertThrows(DataIntegrityViolationException.class, () -> grupoRepository.saveAndFlush(sinResponsable));
    }
}
