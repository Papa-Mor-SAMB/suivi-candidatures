package com.papa.suivicandidatures.service;

import com.papa.suivicandidatures.dto.AuthResponse;
import com.papa.suivicandidatures.dto.LoginRequest;
import com.papa.suivicandidatures.dto.RegisterRequest;
import com.papa.suivicandidatures.model.User;
import com.papa.suivicandidatures.repository.UserRepository;
import com.papa.suivicandidatures.security.JwtUtil;
import com.papa.suivicandidatures.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        passwordEncoder = new BCryptPasswordEncoder();
        loginAttemptService = new LoginAttemptService();
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, loginAttemptService);

        when(jwtUtil.genererToken(anyString())).thenReturn("token-test");
    }

    private User creerUser(String email, String motDePasse) {
        User user = new User();
        user.setNom("Papa");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(motDePasse));
        return user;
    }

    @Test
    void registerHacheLeMotDePasse() {
        RegisterRequest request = new RegisterRequest();
        request.setNom("Papa");
        request.setEmail("Papa@Test.com");
        request.setPassword("MotDePasse1!");

        authService.register(request);

        verify(userRepository).save(argThat((User user) ->
                user.getEmail().equals("papa@test.com")
                        && !user.getPassword().equals("MotDePasse1!")
                        && passwordEncoder.matches("MotDePasse1!", user.getPassword())));
    }

    @Test
    void registerRefuseUnEmailDejaUtilise() {
        when(userRepository.findByEmail("papa@test.com")).thenReturn(creerUser("papa@test.com", "x"));

        RegisterRequest request = new RegisterRequest();
        request.setNom("Papa");
        request.setEmail("papa@test.com");
        request.setPassword("MotDePasse1!");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReussiRenvoieUnToken() {
        when(userRepository.findByEmail("papa@test.com")).thenReturn(creerUser("papa@test.com", "MotDePasse1!"));

        LoginRequest request = new LoginRequest();
        request.setEmail("papa@test.com");
        request.setPassword("MotDePasse1!");

        AuthResponse response = authService.login(request);
        assertEquals("token-test", response.getToken());
    }

    @Test
    void loginAvecMauvaisMotDePasseRenvoie401() {
        when(userRepository.findByEmail("papa@test.com")).thenReturn(creerUser("papa@test.com", "MotDePasse1!"));

        LoginRequest request = new LoginRequest();
        request.setEmail("papa@test.com");
        request.setPassword("mauvais");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void loginBloqueApresCinqEchecsMemeAvecLeBonMotDePasse() {
        when(userRepository.findByEmail("papa@test.com")).thenReturn(creerUser("papa@test.com", "MotDePasse1!"));

        LoginRequest mauvais = new LoginRequest();
        mauvais.setEmail("papa@test.com");
        mauvais.setPassword("mauvais");
        for (int i = 0; i < 5; i++) {
            assertThrows(ResponseStatusException.class, () -> authService.login(mauvais));
        }

        LoginRequest bon = new LoginRequest();
        bon.setEmail("papa@test.com");
        bon.setPassword("MotDePasse1!");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(bon));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }
}
