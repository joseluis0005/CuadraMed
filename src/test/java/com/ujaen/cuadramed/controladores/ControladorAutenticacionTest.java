package com.ujaen.cuadramed.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControladorAutenticacionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void endpointResponde() throws Exception {
        mockMvc.perform(get("/api/auth/ping"))   // pon aquí una ruta tuya real
                .andExpect(status().isOk());
    }

    @Test
    void endPointLogin() throws Exception {
        String peticionJSON = """
                {
                  "email": "admin@cuadramed.es",
                  "password": "admin123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionJSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }
}