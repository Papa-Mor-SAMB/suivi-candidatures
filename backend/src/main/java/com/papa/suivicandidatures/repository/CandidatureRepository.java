package com.papa.suivicandidatures.repository;

import com.papa.suivicandidatures.model.Candidature;
import com.papa.suivicandidatures.model.Statut;
import com.papa.suivicandidatures.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findByUserOrderByDateCandidatureDesc(User user);

    List<Candidature> findByUserAndStatutOrderByDateCandidatureDesc(User user, Statut statut);

    Candidature findByIdAndUser(Long id, User user);

    // candidatures dont la date de relance est passée (ou aujourd'hui) et qui attendent encore une réponse
    List<Candidature> findByUserAndDateRelanceLessThanEqualAndStatutInOrderByDateRelanceAsc(
            User user, LocalDate date, List<Statut> statuts);
}
