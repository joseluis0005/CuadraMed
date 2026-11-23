package com.ujaen.cuadramed.controladores;

import com.jayway.jsonpath.JsonPath;
import com.ujaen.cuadramed.model.Rol;
import com.ujaen.cuadramed.model.Usuario;
import com.ujaen.cuadramed.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de la API de grupos (HU-04): petición HTTP real con token JWT,
 * seguridad por roles, servicio, base de datos y traducción de excepciones a códigos HTTP.
 *
 * Usuarios de DataInitializer: admin@cuadramed.es (ADMINISTRADOR), responsable@cuadramed.es y
 * responsable2@cuadramed.es (PLANIFICADOR + MEDICO), medico@cuadramed.es y medico2@cuadramed.es (MEDICO).
 */
@SpringBootTest
@ActiveProfiles("test") //BD H2 en memoria: no toca la de desarrollo
@AutoConfigureMockMvc
@Transactional //Para que no se pisen unos tests con otros
public class ControladorGrupoTest {

    private static final long ID_INEXISTENTE = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String tokenAdmin;
    private String tokenPlanificador;
    private String tokenMedico;

    private Long idAdmin;
    private Long idAna;
    private Long idLaura;
    private Long idMarcos;
    private Long idPedro;

    @BeforeEach
    void setUp() throws Exception {
        tokenAdmin = tokenDe("admin@cuadramed.es", "admin123");
        tokenPlanificador = tokenDe("responsable@cuadramed.es", "resp123");
        tokenMedico = tokenDe("medico@cuadramed.es", "medico123");

        // Los ids se buscan por email para no depender del orden de inserción
        idAdmin = idDe("admin@cuadramed.es");
        idAna = idDe("responsable@cuadramed.es");
        idLaura = idDe("responsable2@cuadramed.es");
        idMarcos = idDe("medico@cuadramed.es");
        idPedro = idDe("medico2@cuadramed.es");
    }

    // ==================== Utilidades ====================

    private String tokenDe(String email, String password) throws Exception {
        String body = """
            { "email": "%s", "password": "%s" }
            """.formatted(email, password);
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(respuesta, "$.token");
    }

    private Long idDe(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow().getId();
    }

    private String jsonAlta(String nombre, String especialidad, Long idResponsable) {
        return """
            { "nombre": "%s", "especialidad": "%s", "idResponsable": %d }
            """.formatted(nombre, especialidad, idResponsable);
    }

    // Crea un grupo como administrador y devuelve su id
    private Long crearGrupo(String nombre, String especialidad, Long idResponsable) throws Exception {
        String respuesta = mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta(nombre, especialidad, idResponsable)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private void anadirMiembro(Long idGrupo, Long idMedico) throws Exception {
        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", idGrupo, idMedico)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk());
    }

    // ==================== POST /api/grupo/alta ====================

    @Test
    void adminCreaGrupoYElResponsableQuedaComoMiembro() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", idAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Urgencias"))
                .andExpect(jsonPath("$.especialidad").value("URGENCIAS"))
                .andExpect(jsonPath("$.responsable.id").value(idAna))
                .andExpect(jsonPath("$.responsable.email").value("responsable@cuadramed.es"))
                .andExpect(jsonPath("$.miembros", hasSize(1)))
                .andExpect(jsonPath("$.miembros[0].id").value(idAna));
    }

    @Test
    void laRespuestaDelGrupoNoExponeDatosSensiblesDelUsuario() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", idAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsable.contrasena").doesNotExist())
                .andExpect(jsonPath("$.responsable.roles").doesNotExist())
                .andExpect(jsonPath("$.miembros[0].contrasena").doesNotExist());
    }

    @Test
    void planificadorNoPuedeCrearGrupos() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenPlanificador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", idAna)))
                .andExpect(status().isForbidden());
    }

    @Test
    void medicoNoPuedeCrearGrupos() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", idAna)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenNoSePuedeCrearGrupo() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", idAna)))
                .andExpect(status().isForbidden());
    }

    @Test
    void altaSinNombreDevuelve400ConMensaje() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nombre": "  ", "especialidad": "URGENCIAS", "idResponsable": %d }
                            """.formatted(idAna)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El nombre es obligatorio"));
    }

    @Test
    void altaSinEspecialidadDevuelve400ConMensaje() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nombre": "Urgencias", "idResponsable": %d }
                            """.formatted(idAna)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La especialidad es obligatoria"));
    }

    @Test
    void altaSinResponsableDevuelve400ConMensaje() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nombre": "Urgencias", "especialidad": "URGENCIAS" }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El responsable es obligatorio"));
    }

    @Test
    void altaConEspecialidadQueNoExisteDevuelve400() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Dermatología", "DERMATOLOGIA", idAna)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void altaConEspecialidadRepetidaDevuelve409() throws Exception {
        crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias 2", "URGENCIAS", idLaura)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void altaConResponsableInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", ID_INEXISTENTE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void altaConResponsableQueNoEsPlanificadorDevuelve400() throws Exception {
        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Urgencias", "URGENCIAS", idMarcos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void altaConResponsableQueYaDirigeOtroGrupoDevuelve409() throws Exception {
        crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Pediatría", "PEDIATRIA", idAna)))
                .andExpect(status().isConflict());
    }

    @Test
    void altaConResponsableQueEsMiembroDeOtroGrupoDevuelve409() throws Exception {
        Long urgencias = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(urgencias, idLaura);

        mockMvc.perform(post("/api/grupo/alta")
                        .header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlta("Pediatría", "PEDIATRIA", idLaura)))
                .andExpect(status().isConflict());
    }

    // ==================== GET /api/grupo ====================

    @Test
    void adminYPlanificadorPuedenListarLosGrupos() throws Exception {
        crearGrupo("Urgencias", "URGENCIAS", idAna);
        crearGrupo("Pediatría", "PEDIATRIA", idLaura);

        mockMvc.perform(get("/api/grupo").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].especialidad", containsInAnyOrder("URGENCIAS", "PEDIATRIA")));

        mockMvc.perform(get("/api/grupo").header("Authorization", tokenPlanificador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listarSinGruposDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/grupo").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void medicoNoPuedeListarGrupos() throws Exception {
        mockMvc.perform(get("/api/grupo").header("Authorization", tokenMedico))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenNoSePuedenListarGrupos() throws Exception {
        mockMvc.perform(get("/api/grupo"))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/grupo/{id}/consulta ====================

    @Test
    void consultarGrupoDevuelveSusDatosYMiembros() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(id, idMarcos);

        mockMvc.perform(get("/api/grupo/{id}/consulta", id).header("Authorization", tokenPlanificador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Urgencias"))
                .andExpect(jsonPath("$.responsable.id").value(idAna))
                .andExpect(jsonPath("$.miembros[*].id", containsInAnyOrder(idAna.intValue(), idMarcos.intValue())));
    }

    @Test
    void consultarGrupoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/grupo/{id}/consulta", ID_INEXISTENTE).header("Authorization", tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void medicoNoPuedeConsultarUnGrupo() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(get("/api/grupo/{id}/consulta", id).header("Authorization", tokenMedico))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/grupo/{id} ====================

    @Test
    void adminEliminaGrupoYYaNoSePuedeConsultar() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(delete("/api/grupo/{id}", id).header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/grupo/{id}/consulta", id).header("Authorization", tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void trasEliminarUnGrupoSusMedicosPuedenEntrarEnOtro() throws Exception {
        Long urgencias = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(urgencias, idMarcos);
        mockMvc.perform(delete("/api/grupo/{id}", urgencias).header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        Long pediatria = crearGrupo("Pediatría", "PEDIATRIA", idAna);
        anadirMiembro(pediatria, idMarcos);
    }

    @Test
    void eliminarGrupoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/grupo/{id}", ID_INEXISTENTE).header("Authorization", tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void planificadorNoPuedeEliminarGrupos() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(delete("/api/grupo/{id}", id).header("Authorization", tokenPlanificador))
                .andExpect(status().isForbidden());
    }

    // ==================== POST /api/grupo/{id}/miembros/{idMedico} ====================

    @Test
    void planificadorAnadeMedicosAlGrupo() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miembros", hasSize(2)))
                .andExpect(jsonPath("$.miembros[*].id", hasItem(idMarcos.intValue())));

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idPedro)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miembros", hasSize(3)));
    }

    @Test
    void anadirMiembroAGrupoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", ID_INEXISTENTE, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isNotFound());
    }

    @Test
    void anadirMedicoInexistenteDevuelve404() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, ID_INEXISTENTE)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isNotFound());
    }

    @Test
    void anadirUsuarioQueNoEsMedicoDevuelve400() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idAdmin)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void anadirMedicoDadoDeBajaDevuelve400() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        mockMvc.perform(put("/api/medico/{id}/baja", idMarcos).header("Authorization", tokenPlanificador))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void anadirDosVecesElMismoMedicoDevuelve409() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(id, idMarcos);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isConflict());
    }

    @Test
    void unMedicoNoPuedeEstarEnDosGrupos() throws Exception {
        Long urgencias = crearGrupo("Urgencias", "URGENCIAS", idAna);
        Long pediatria = crearGrupo("Pediatría", "PEDIATRIA", idLaura);
        anadirMiembro(urgencias, idMarcos);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", pediatria, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El medico ya pertenece a otro grupo"));
    }

    @Test
    void medicoNoPuedeAnadirMiembros() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idPedro)
                        .header("Authorization", tokenMedico))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNoGestionaLosMiembrosDelGrupo() throws Exception {
        // Reparto de permisos actual: el administrador crea grupos, el planificador gestiona sus miembros
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(post("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/grupo/{id}/miembros/{idMedico} ====================

    @Test
    void planificadorQuitaMedicosDelGrupo() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(id, idMarcos);

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miembros", hasSize(1)))
                .andExpect(jsonPath("$.miembros[*].id", not(hasItem(idMarcos.intValue()))));
    }

    @Test
    void unMedicoQuitadoDeUnGrupoPuedeEntrarEnOtro() throws Exception {
        Long urgencias = crearGrupo("Urgencias", "URGENCIAS", idAna);
        Long pediatria = crearGrupo("Pediatría", "PEDIATRIA", idLaura);
        anadirMiembro(urgencias, idMarcos);

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", urgencias, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk());

        anadirMiembro(pediatria, idMarcos);
    }

    @Test
    void noSePuedeQuitarAlResponsableDelGrupo() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, idAna)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void quitarMedicoQueNoEstaEnElGrupoDevuelve404() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isNotFound());
    }

    @Test
    void quitarMiembroDeGrupoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", ID_INEXISTENTE, idMarcos)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isNotFound());
    }

    @Test
    void quitarMedicoInexistenteDevuelve404() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, ID_INEXISTENTE)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isNotFound());
    }

    @Test
    void medicoNoPuedeQuitarMiembros() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(id, idMarcos);

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, idMarcos)
                        .header("Authorization", tokenMedico))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /api/grupo/{id}/responsable/{idUsuario} ====================

    @Test
    void adminCambiaElResponsableYElAnteriorSigueComoMiembro() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, idLaura)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsable.id").value(idLaura))
                .andExpect(jsonPath("$.miembros[*].id", containsInAnyOrder(idAna.intValue(), idLaura.intValue())));
    }

    @Test
    void trasCambiarElResponsableSePuedeQuitarAlAnterior() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, idLaura)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, idAna)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miembros[*].id", not(hasItem(idAna.intValue()))));
    }

    @Test
    void cambiarResponsablePorElMismoDevuelveElGrupoSinCambios() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, idAna)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsable.id").value(idAna))
                .andExpect(jsonPath("$.miembros", hasSize(1)));
    }

    @Test
    void cambiarResponsableAUnMiembroDelPropioGrupoNoLoDuplica() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(id, idLaura);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, idLaura)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsable.id").value(idLaura))
                .andExpect(jsonPath("$.miembros", hasSize(2)));
    }

    @Test
    void cambiarResponsableAQuienYaDirigeOtroGrupoDevuelve409() throws Exception {
        Long urgencias = crearGrupo("Urgencias", "URGENCIAS", idAna);
        crearGrupo("Pediatría", "PEDIATRIA", idLaura);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", urgencias, idLaura)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void cambiarResponsableAMiembroDeOtroGrupoDevuelve409() throws Exception {
        // Tercer planificador para poder tener dos grupos y a Laura como simple miembro de uno
        Usuario carlos = usuarioRepository.save(new Usuario("Carlos", "600999888", "carlos@cuadramed.es",
                "pass", Set.of(Rol.PLANIFICADOR, Rol.MEDICO)));
        Long urgencias = crearGrupo("Urgencias", "URGENCIAS", idAna);
        Long pediatria = crearGrupo("Pediatría", "PEDIATRIA", carlos.getId());
        anadirMiembro(urgencias, idLaura);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", pediatria, idLaura)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isConflict());
    }

    @Test
    void cambiarResponsableAUsuarioQueNoEsPlanificadorDevuelve400() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, idMarcos)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isBadRequest());

        // El grupo no ha cambiado
        mockMvc.perform(get("/api/grupo/{id}/consulta", id).header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.responsable.id").value(idAna))
                .andExpect(jsonPath("$.miembros", hasSize(1)));
    }

    @Test
    void cambiarResponsableDeGrupoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", ID_INEXISTENTE, idLaura)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarResponsableAUsuarioInexistenteDevuelve404() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, ID_INEXISTENTE)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void planificadorNoPuedeCambiarElResponsable() throws Exception {
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);

        mockMvc.perform(put("/api/grupo/{id}/responsable/{idUsuario}", id, idLaura)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isForbidden());
    }

    // ==================== Flujo completo de la HU-04 ====================

    @Test
    void flujoCompletoCrearGrupoAnadirYQuitarMedicos() throws Exception {
        // Criterio 1: un grupo tiene nombre, responsable y médicos
        Long id = crearGrupo("Urgencias", "URGENCIAS", idAna);
        anadirMiembro(id, idMarcos);
        anadirMiembro(id, idPedro);

        mockMvc.perform(get("/api/grupo/{id}/consulta", id).header("Authorization", tokenPlanificador))
                .andExpect(jsonPath("$.nombre").value("Urgencias"))
                .andExpect(jsonPath("$.responsable.id").value(idAna))
                .andExpect(jsonPath("$.miembros[*].id",
                        containsInAnyOrder(idAna.intValue(), idMarcos.intValue(), idPedro.intValue())));

        // Criterio 2: se pueden añadir y quitar médicos del grupo
        mockMvc.perform(delete("/api/grupo/{id}/miembros/{idMedico}", id, idPedro)
                        .header("Authorization", tokenPlanificador))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/grupo/{id}/consulta", id).header("Authorization", tokenPlanificador))
                .andExpect(jsonPath("$.miembros[*].id", containsInAnyOrder(idAna.intValue(), idMarcos.intValue())));
    }
}
