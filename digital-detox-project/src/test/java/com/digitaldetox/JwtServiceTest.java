package com.digitaldetox;

import com.digitaldetox.authentication.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generatesTokenForAdmin() {
        String token = jwtService.generateToken("admin", "ADMIN");
        assertNotNull(token);
    }

    @Test
    void generatedTokenContainsSubjectAndRole() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertEquals("admin", jwtService.extractSubject(token));
        assertEquals("ADMIN", jwtService.extractClaim(token, claims -> claims.get("role", String.class)));
        assertTrue(jwtService.isTokenValid(token, userDetails("admin")));
        assertFalse(jwtService.isTokenValid(token, userDetails("someone-else")));
    }

    private static UserDetails userDetails(String username) {
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("unused")
                .authorities("ROLE_ADMIN")
                .build();
    }
}
