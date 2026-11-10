package com.ujaen.cuadramed.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControladorMedicoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void listarMedicosDebeDevolverListaJSON() throws Exception {
        mockMvc.perform(get("/api/medico").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void altaMedicoExitosoDebeDevolverRespuestaAutenticacion() throws Exception {
        String jsonPeticion = """
                {
                  "nombre": "Dr. Carlos Ruiz",
                  "especialidad": "URGENCIAS",
                  "email": "carlos.ruiz@cuadramed.es",
                  "contrasena": "medico123",
                  "telefono": "655443322"
                }
                """;

        mockMvc.perform(post("/api/medico/alta").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPeticion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("MEDICO"));
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void bajaMedicoExistenteDebeDevolver204NoContent() throws Exception {
        // Usa el ID 3 de 'Marcos Médico' precargado por DataInitializer
        mockMvc.perform(put("/api/medico/3/baja").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE")
    void bajaMedicoInexistenteDebeDevolver404NotFound() throws Exception {
        mockMvc.perform(put("/api/medico/999/baja").with(csrf()))
                .andExpect(status().isNotFound());
    }
}