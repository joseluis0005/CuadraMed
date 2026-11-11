package com.ujaen.cuadramed.controladores;

import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional //Para que no se pisen unos tests con otros
public class ControladorMedicoTest {

    @Autowired
    private MockMvc mockMvc;

    private String tokenDe(String email, String password) throws Exception {
        String body = """
            { "email": "%s", "password": "%s" }
            """.formatted(email, password);
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private String tokenPlanificador() throws Exception {
        return "Bearer " + tokenDe("responsable@cuadramed.es", "resp123");
    }

    private String tokenMedico() throws Exception {
        return "Bearer " + tokenDe("medico@cuadramed.es", "medico123");
    }

    @Test
    void listarMedicosDebeDevolverListaJSON() throws Exception {
        mockMvc.perform(get("/api/medico")
                        .header("Authorization", tokenPlanificador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void altaMedicoExitosoDebeDevolverElMedicoCreado() throws Exception {
        String jsonPeticion = """
            {
              "nombre": "Dr. Carlos Ruiz",
              "especialidad": "URGENCIAS",
              "email": "carlos.ruiz@cuadramed.es",
              "contrasena": "medico123",
              "telefono": "655443322"
            }
            """;

        mockMvc.perform(post("/api/medico/alta")
                        .header("Authorization", tokenPlanificador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPeticion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("carlos.ruiz@cuadramed.es"))
                .andExpect(jsonPath("$.nombre").value("Dr. Carlos Ruiz"))
                .andExpect(jsonPath("$.token").doesNotExist());


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "email": "carlos.ruiz@cuadramed.es", "password": "medico123" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol", hasItem("MEDICO")));
    }

    @Test
    void bajaMedicoExistenteDebeDevolver204NoContent() throws Exception {
        mockMvc.perform(put("/api/medico/3/baja")
                        .header("Authorization", tokenPlanificador()))
                .andExpect(status().isNoContent());
    }

    @Test
    void bajaMedicoInexistenteDebeDevolver404NotFound() throws Exception {
        mockMvc.perform(put("/api/medico/999/baja")
                        .header("Authorization", tokenPlanificador()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void medicoSinRolPlanificadorNoPuedeListarMedicos() throws Exception {
        mockMvc.perform(get("/api/medico")
                        .header("Authorization", tokenMedico()))
                .andExpect(status().isForbidden());
    }

    @Test
    void medicoSinRolPlanificadorNoPuedeDarDeBaja() throws Exception {
        mockMvc.perform(put("/api/medico/3/baja")
                        .header("Authorization", tokenMedico()))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenNoSePuedeListarMedicos() throws Exception {
        mockMvc.perform(get("/api/medico"))
                .andExpect(status().isForbidden());
    }
}