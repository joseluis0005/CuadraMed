package com.ujaen.cuadramed.seguridad;

import com.ujaen.cuadramed.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtils {

    @Value("${jwt.secret:c3VwZXItc2VjcmV0LWtleS1mb3ItY3VhZGFtZWQtand0LWF1dGhlbnRpY2F0aW9uLXNlcnZpY2U=}") //Llave con más de 32 caracteres
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long id, Set<Rol> roles, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles.stream().map(Rol::name).toList())
                .claim("id", id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getRolFromToken(String token) {
        return getClaims(token).get("rol", String.class);
    }
}