package com.ujaen.cuadramed.seguridad;

import com.ujaen.cuadramed.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretKey", "c3VwZXItc2VjcmV0LWtleS1mb3ItY3VhZGFtZWQtand0LWF1dGhlbnRpY2F0aW9uLXNlcnZpY2U=");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000L);
    }



    @Test
    void debeGenerarYValidarTokenCorrectamente() {
        String token = jwtUtils.generateToken(1L, new HashSet<>(Arrays.asList(Rol.ADMINISTRADOR)), "admin@cuadramed.es");
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void debeExtraerEmailYRolesDelToken() {
        String token = jwtUtils.generateToken(1L, Set.of(Rol.PLANIFICADOR, Rol.MEDICO), "responsable@cuadramed.es");

        assertEquals("responsable@cuadramed.es", jwtUtils.getEmailFromToken(token));
        assertEquals(Set.of(Rol.PLANIFICADOR, Rol.MEDICO), jwtUtils.getRolesFromToken(token));
    }

    @Test
    void debeRetornarFalseSiElTokenEsInvalido() {
        assertFalse(jwtUtils.validateToken("token.invalido.falso"));
    }
}