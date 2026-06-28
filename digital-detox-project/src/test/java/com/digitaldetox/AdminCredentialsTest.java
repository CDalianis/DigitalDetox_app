package com.digitaldetox;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminCredentialsTest {

    private static final String ADMIN_HASH =
            "$2a$12$zYXkjE.GrNWxgFIr5fGVN.nTqSTy239ZNvb6kaEnBKexqedzX9Y/K";

    @Test
    void seededAdminPasswordMatchesAdmin123() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        assertTrue(encoder.matches("Admin123!", ADMIN_HASH));
    }
}
