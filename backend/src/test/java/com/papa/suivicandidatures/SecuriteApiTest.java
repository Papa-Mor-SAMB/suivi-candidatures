package com.papa.suivicandidatures;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de bout en bout de la sécurité de l'API (base H2 en mémoire).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecuriteApiTest {

    @Autowired
    private MockMvc mockMvc;

    private String inscrire(String email) throws Exception {
        String json = "{\"nom\":\"Papa\",\"email\":\"" + email + "\",\"password\":\"MotDePasse1!\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // on récupère la valeur du token dans {"token":"...","nom":"..."}
        int debut = body.indexOf("\"token\":\"") + 9;
        int fin = body.indexOf("\"", debut);
        return body.substring(debut, fin);
    }

    @Test
    void sansTokenLApiRenvoie401() throws Exception {
        mockMvc.perform(get("/api/candidatures"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void avecUnFauxTokenLApiRenvoie401() throws Exception {
        mockMvc.perform(get("/api/candidatures").header("Authorization", "Bearer faux.token.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void motDePasseTropFaibleEstRefuse() throws Exception {
        String json = "{\"nom\":\"Papa\",\"email\":\"faible@test.com\",\"password\":\"123456\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unUtilisateurNeVoitPasLesCandidaturesDUnAutre() throws Exception {
        String tokenA = inscrire("a@test.com");
        String tokenB = inscrire("b@test.com");

        String candidature = "{\"entreprise\":\"Capgemini\",\"poste\":\"Stage Java\",\"statut\":\"ENVOYEE\"}";
        MvcResult result = mockMvc.perform(post("/api/candidatures")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(candidature))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        int debut = body.indexOf("\"id\":") + 5;
        int fin = body.indexOf(",", debut);
        String id = body.substring(debut, fin);

        // B essaie de lire la candidature de A
        mockMvc.perform(get("/api/candidatures/" + id).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void connexionBloqueeApresCinqEchecs() throws Exception {
        inscrire("brute@test.com");
        String mauvais = "{\"email\":\"brute@test.com\",\"password\":\"mauvais\"}";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(mauvais))
                    .andExpect(status().isUnauthorized());
        }

        String bon = "{\"email\":\"brute@test.com\",\"password\":\"MotDePasse1!\"}";
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(bon))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void lesEnTetesDeSecuriteSontPresents() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/candidatures")).andReturn();
        String csp = result.getResponse().getHeader("Content-Security-Policy");
        assertTrue(csp != null && csp.contains("default-src 'none'"));
        assertTrue("DENY".equals(result.getResponse().getHeader("X-Frame-Options")));
    }
}
