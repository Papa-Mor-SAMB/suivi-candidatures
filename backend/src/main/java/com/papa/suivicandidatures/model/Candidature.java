package com.papa.suivicandidatures.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'entreprise est obligatoire")
    private String entreprise;

    @NotBlank(message = "Le poste est obligatoire")
    private String poste;

    private String lieu;

    private LocalDate dateCandidature;

    private LocalDate dateRelance;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    private Statut statut;

    private String lienOffre;

    @Column(length = 2000)
    private String notes;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Candidature() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }

    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public LocalDate getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(LocalDate dateCandidature) { this.dateCandidature = dateCandidature; }

    public LocalDate getDateRelance() { return dateRelance; }
    public void setDateRelance(LocalDate dateRelance) { this.dateRelance = dateRelance; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getLienOffre() { return lienOffre; }
    public void setLienOffre(String lienOffre) { this.lienOffre = lienOffre; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
