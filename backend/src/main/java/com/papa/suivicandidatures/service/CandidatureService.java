package com.papa.suivicandidatures.service;

import com.papa.suivicandidatures.model.Candidature;
import com.papa.suivicandidatures.model.Statut;
import com.papa.suivicandidatures.model.User;
import com.papa.suivicandidatures.repository.CandidatureRepository;
import com.papa.suivicandidatures.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CandidatureService {

    // délai par défaut avant de relancer une entreprise
    public static final int JOURS_AVANT_RELANCE = 10;
    // quand on vient de relancer, on reprogramme la relance suivante
    public static final int JOURS_ENTRE_RELANCES = 7;

    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;

    public CandidatureService(CandidatureRepository candidatureRepository, UserRepository userRepository) {
        this.candidatureRepository = candidatureRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable");
        }
        return user;
    }

    public List<Candidature> getAll(String email, Statut statut) {
        User user = getUser(email);
        if (statut == null) {
            return candidatureRepository.findByUserOrderByDateCandidatureDesc(user);
        }
        return candidatureRepository.findByUserAndStatutOrderByDateCandidatureDesc(user, statut);
    }

    public Candidature getById(String email, Long id) {
        User user = getUser(email);
        Candidature candidature = candidatureRepository.findByIdAndUser(id, user);
        if (candidature == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable");
        }
        return candidature;
    }

    public Candidature create(String email, Candidature candidature) {
        User user = getUser(email);
        candidature.setId(null);
        candidature.setUser(user);
        if (candidature.getDateCandidature() == null) {
            candidature.setDateCandidature(LocalDate.now());
        }
        if (candidature.getDateRelance() == null && candidature.getStatut() == Statut.ENVOYEE) {
            candidature.setDateRelance(candidature.getDateCandidature().plusDays(JOURS_AVANT_RELANCE));
        }
        return candidatureRepository.save(candidature);
    }

    public Candidature update(String email, Long id, Candidature nouvelle) {
        Candidature existante = getById(email, id);

        existante.setEntreprise(nouvelle.getEntreprise());
        existante.setPoste(nouvelle.getPoste());
        existante.setLieu(nouvelle.getLieu());
        existante.setDateCandidature(nouvelle.getDateCandidature());
        existante.setDateRelance(nouvelle.getDateRelance());
        existante.setStatut(nouvelle.getStatut());
        existante.setLienOffre(nouvelle.getLienOffre());
        existante.setNotes(nouvelle.getNotes());

        // une candidature terminée n'a plus besoin de relance
        if (existante.getStatut() == Statut.REFUSEE || existante.getStatut() == Statut.ACCEPTEE) {
            existante.setDateRelance(null);
        }

        return candidatureRepository.save(existante);
    }

    public void delete(String email, Long id) {
        Candidature candidature = getById(email, id);
        candidatureRepository.delete(candidature);
    }

    public List<Candidature> getRelances(String email) {
        User user = getUser(email);
        List<Statut> enAttente = new ArrayList<>();
        enAttente.add(Statut.ENVOYEE);
        enAttente.add(Statut.ENTRETIEN);

        return candidatureRepository.findByUserAndDateRelanceLessThanEqualAndStatutInOrderByDateRelanceAsc(
                user, LocalDate.now(), enAttente);
    }

    public Candidature marquerRelancee(String email, Long id) {
        Candidature candidature = getById(email, id);
        candidature.setDateRelance(LocalDate.now().plusDays(JOURS_ENTRE_RELANCES));
        return candidatureRepository.save(candidature);
    }

    public Map<String, Integer> getStats(String email) {
        List<Candidature> candidatures = candidatureRepository.findByUserOrderByDateCandidatureDesc(getUser(email));

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("TOTAL", candidatures.size());
        for (Statut s : Statut.values()) {
            stats.put(s.name(), 0);
        }
        for (Candidature c : candidatures) {
            String cle = c.getStatut().name();
            stats.put(cle, stats.get(cle) + 1);
        }
        return stats;
    }
}
