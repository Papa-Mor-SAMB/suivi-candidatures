package com.papa.suivicandidatures.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    void pasBloqueAvantCinqEchecs() {
        for (int i = 0; i < 4; i++) {
            service.echec("test@mail.com");
        }
        assertFalse(service.estBloque("test@mail.com"));
    }

    @Test
    void bloqueApresCinqEchecs() {
        for (int i = 0; i < 5; i++) {
            service.echec("test@mail.com");
        }
        assertTrue(service.estBloque("test@mail.com"));
    }

    @Test
    void blocageIgnoreLesMajuscules() {
        for (int i = 0; i < 5; i++) {
            service.echec("Test@Mail.com");
        }
        assertTrue(service.estBloque("test@mail.com"));
    }

    @Test
    void succesRemetLeCompteurAZero() {
        for (int i = 0; i < 4; i++) {
            service.echec("test@mail.com");
        }
        service.succes("test@mail.com");
        service.echec("test@mail.com");
        assertFalse(service.estBloque("test@mail.com"));
    }
}
