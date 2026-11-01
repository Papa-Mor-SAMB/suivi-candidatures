package com.papa.suivicandidatures.service;

import com.papa.suivicandidatures.model.Candidature;
import com.papa.suivicandidatures.model.Statut;
import com.papa.suivicandidatures.model.User;
import com.papa.suivicandidatures.repository.CandidatureRepository;
import com.papa.suivicandidatures.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CandidatureServiceTest {

    private CandidatureRepository candidatureRepository;
    private UserRepository userRepository;
    private CandidatureService service;
    private User papa;

    @BeforeEach
    void setUp() {
        candidatureRepository = mock(CandidatureRepository.class);
        userRepository = mock(UserRepository.class);
        service = new CandidatureService(candidatureRepository, userRepository);

        papa = new User();
        papa.setId(1L);
        papa.setEmail("papa@test.com");
        when(userRepository.findByEmail("papa@test.com")).thenReturn(papa);

        // save renvoie simplement l'objet reçu
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Candidature candidature(String entreprise, Statut statut) {
        Candidature c = new Candidature();
        c.setEntreprise(entreprise);
        c.setPoste("Stage Java");
        c.setStatut(statut);
        return c;
    }

    @Test
    void createRattacheLaCandidatureALUtilisateurConnecte() {
        Candidature c = candidature("Capgemini", Statut.ENVOYEE);
        c.setId(99L); // un id envoyé par le client doit être ignoré

        Candidature resultat = service.create("papa@test.com", c);

        assertEquals(papa, resultat.getUser());
        assertNull(resultat.getId());
    }

    @Test
    void createProgrammeUneRelanceDixJoursApres() {
        Candidature c = candidature("Capgemini", Statut.ENVOYEE);
        c.setDateCandidature(LocalDate.of(2026, 11, 1));

        Candidature resultat = service.create("papa@test.com", c);

        assertEquals(LocalDate.of(2026, 11, 11), resultat.getDateRelance());
    }

    @Test
    void createMetLaDateDuJourSiAbsente() {
        Candidature resultat = service.create("papa@test.com", candidature("Sopra", Statut.ENVOYEE));
        assertEquals(LocalDate.now(), resultat.getDateCandidature());
    }

    @Test
    void getByIdRefuseLaCandidatureDUnAutreUtilisateur() {
        // le repository ne trouve rien pour (id, papa) : la candidature appartient à quelqu'un d'autre
        when(candidatureRepository.findByIdAndUser(5L, papa)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getById("papa@test.com", 5L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateSupprimeLaRelanceQuandLaCandidatureEstRefusee() {
        Candidature existante = candidature("Capgemini", Statut.ENVOYEE);
        existante.setDateRelance(LocalDate.now());
        when(candidatureRepository.findByIdAndUser(1L, papa)).thenReturn(existante);

        Candidature modif = candidature("Capgemini", Statut.REFUSEE);
        modif.setDateRelance(LocalDate.now());

        Candidature resultat = service.update("papa@test.com", 1L, modif);

        assertEquals(Statut.REFUSEE, resultat.getStatut());
        assertNull(resultat.getDateRelance());
    }

    @Test
    void marquerRelanceeReporteDeSeptJours() {
        Candidature existante = candidature("Capgemini", Statut.ENVOYEE);
        when(candidatureRepository.findByIdAndUser(1L, papa)).thenReturn(existante);

        Candidature resultat = service.marquerRelancee("papa@test.com", 1L);

        assertEquals(LocalDate.now().plusDays(7), resultat.getDateRelance());
    }

    @Test
    void statsCompteParStatut() {
        List<Candidature> liste = new ArrayList<>();
        liste.add(candidature("A", Statut.ENVOYEE));
        liste.add(candidature("B", Statut.ENVOYEE));
        liste.add(candidature("C", Statut.ENTRETIEN));
        when(candidatureRepository.findByUserOrderByDateCandidatureDesc(papa)).thenReturn(liste);

        Map<String, Integer> stats = service.getStats("papa@test.com");

        assertEquals(3, stats.get("TOTAL"));
        assertEquals(2, stats.get("ENVOYEE"));
        assertEquals(1, stats.get("ENTRETIEN"));
        assertEquals(0, stats.get("REFUSEE"));
    }
}
