package com.papa.suivicandidatures.dto;

public class AuthResponse {

    private String token;
    private String nom;

    public AuthResponse(String token, String nom) {
        this.token = token;
        this.nom = nom;
    }

    public String getToken() { return token; }
    public String getNom() { return nom; }
}
