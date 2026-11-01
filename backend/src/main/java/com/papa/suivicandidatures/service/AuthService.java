package com.papa.suivicandidatures.service;

import com.papa.suivicandidatures.dto.AuthResponse;
import com.papa.suivicandidatures.dto.LoginRequest;
import com.papa.suivicandidatures.dto.RegisterRequest;
import com.papa.suivicandidatures.model.User;
import com.papa.suivicandidatures.repository.UserRepository;
import com.papa.suivicandidatures.security.JwtUtil;
import com.papa.suivicandidatures.security.LoginAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }

        User user = new User();
        user.setNom(request.getNom().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtUtil.genererToken(user.getEmail());
        return new AuthResponse(token, user.getNom());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (loginAttemptService.estBloque(email)) {
            long minutes = loginAttemptService.minutesRestantes(email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Trop de tentatives de connexion. Réessaie dans " + minutes + " minute(s).");
        }

        User user = userRepository.findByEmail(email);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.echec(email);
            // même message que l'email existe ou non : on ne révèle pas quels comptes existent
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }

        loginAttemptService.succes(email);
        String token = jwtUtil.genererToken(user.getEmail());
        return new AuthResponse(token, user.getNom());
    }
}
