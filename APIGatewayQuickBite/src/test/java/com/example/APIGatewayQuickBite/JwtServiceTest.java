package com.example.APIGatewayQuickBite;

import com.example.APIGatewayQuickBite.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Un secreto de prueba largo (mínimo 256 bits / 32 bytes para algoritmos HMAC)
    private final String secretPrueba = "anSecretoSuperSeguroYMuyLargoParaQuickBite2026!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inyectamos manualmente el string 'secret' en la propiedad privada del servicio
        ReflectionTestUtils.setField(jwtService, "secret", secretPrueba);
    }

    // Método auxiliar para generar tokens válidos dentro del test
    private String generarTokenPrueba(String subject, Map<String, Object> claimsExtra) {
        SecretKey key = Keys.hmacShaKeyFor(secretPrueba.getBytes());
        return Jwts.builder()
                .claims(claimsExtra)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora de validez
                .signWith(key)
                .compact();
    }

    @Test
    void cuandoElTokenEsValido_debeExtraerEmailCorrectamente() {
        String emailEsperado = "usuario@quickbite.com";
        String token = generarTokenPrueba(emailEsperado, Map.of("role", "ROLE_USER"));

        String emailExtraido = jwtService.extractEmail(token);

        assertEquals(emailEsperado, emailExtraido);

    }

    @Test
    void cuandoElTokenTieneRol_debeExtraerRolCorrectamente() {
        String rolEsperado = "ROLE_ADMIN";
        String token = generarTokenPrueba("admin@quickbite.com", Map.of("role", rolEsperado));

        String rolExtraido = jwtService.extractRole(token);

        assertEquals(rolEsperado, rolExtraido);
    }

    @Test
    void cuandoElTokenEsCorrecto_isTokenValid_debeRetornarTrue() {
        String token = generarTokenPrueba("test@quickbite.com", Map.of("role", "ROLE_USER"));

        boolean esValido = jwtService.isTokenValid(token);

        assertTrue(esValido);
    }

    @Test
    void cuandoElTokenEsInvalidoOMalformado_isTokenValid_debeRetornarFalse() {
        String tokenInvalido = "un.token.completamenteInvalido";

        boolean esValido = jwtService.isTokenValid(tokenInvalido);

        assertFalse(esValido);
    }

    @Test
    void cuandoElTokenEsFirmadoConOtroSecreto_isTokenValid_debeRetornarFalse() {
        SecretKey claveErronea = Keys.hmacShaKeyFor("otraClaveCompletamenteDiferenteYFalsa!".getBytes());
        String tokenConClaveFalsa = Jwts.builder()
                .subject("hacker@quickbite.com")
                .signWith(claveErronea)
                .compact();

        boolean esValido = jwtService.isTokenValid(tokenConClaveFalsa);

        assertFalse(esValido);
    }
}