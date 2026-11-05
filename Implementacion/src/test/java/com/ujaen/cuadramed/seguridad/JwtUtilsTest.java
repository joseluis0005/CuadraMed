package com.ujaen.cuadramed.seguridad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
        String token = jwtUtils.generateToken(1L, "ADMINISTRADOR", "admin@cuadramed.es");
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void debeExtraerEmailYRolDelToken() {
        String token = jwtUtils.generateToken(1L, "ADMINISTRADOR", "admin@cuadramed.es");
        assertEquals("admin@cuadramed.es", jwtUtils.getEmailFromToken(token));
        assertEquals("ADMINISTRADOR", jwtUtils.getRolFromToken(token));
    }

    @Test
    void debeRetornarFalseSiElTokenEsInvalido() {
        assertFalse(jwtUtils.validateToken("token.invalido.falso"));
    }
}