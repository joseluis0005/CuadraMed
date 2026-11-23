package com.ujaen.cuadramed.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprueba la configuración CORS: el navegador lanza una petición previa OPTIONS sin token
 * antes de llamar a la API desde el frontend, y Spring Security debe dejarla pasar.
 */
@SpringBootTest
@ActiveProfiles("test") //BD H2 en memoria: no toca la de desarrollo
@AutoConfigureMockMvc
class ConfigSeguridadCorsTest {

    private static final String ORIGEN_FRONTEND = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void peticionPreviaDesdeElFrontendEstaPermitidaSinToken() throws Exception {
        mockMvc.perform(options("/api/grupo")
                        .header("Origin", ORIGEN_FRONTEND)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN_FRONTEND));
    }

    @Test
    void peticionPreviaDelLoginDesdeElFrontendEstaPermitida() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGEN_FRONTEND)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN_FRONTEND));
    }

    @Test
    void peticionPreviaDesdeOtroOrigenSeRechaza() throws Exception {
        mockMvc.perform(options("/api/grupo")
                        .header("Origin", "http://sitio-malicioso.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void metodoNoPermitidoSeRechaza() throws Exception {
        mockMvc.perform(options("/api/grupo")
                        .header("Origin", ORIGEN_FRONTEND)
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isForbidden());
    }
}
