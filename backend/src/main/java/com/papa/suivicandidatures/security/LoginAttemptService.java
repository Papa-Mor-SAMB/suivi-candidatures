package com.papa.suivicandidatures.security;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Protection contre la force brute :
 * après 5 échecs de connexion, le compte est bloqué 15 minutes.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_TENTATIVES = 5;
    public static final long DUREE_BLOCAGE_MS = 15 * 60 * 1000;

    private final Map<String, Integer> tentatives = new HashMap<>();
    private final Map<String, Long> bloqueJusqua = new HashMap<>();

    public synchronized boolean estBloque(String email) {
        String cle = email.toLowerCase();
        Long fin = bloqueJusqua.get(cle);
        if (fin == null) {
            return false;
        }
        if (System.currentTimeMillis() > fin) {
            // le blocage est terminé, on remet à zéro
            bloqueJusqua.remove(cle);
            tentatives.remove(cle);
            return false;
        }
        return true;
    }

    public synchronized long minutesRestantes(String email) {
        Long fin = bloqueJusqua.get(email.toLowerCase());
        if (fin == null) {
            return 0;
        }
        long restant = fin - System.currentTimeMillis();
        return (restant / 60000) + 1;
    }

    public synchronized void echec(String email) {
        String cle = email.toLowerCase();
        int nb = 1;
        if (tentatives.containsKey(cle)) {
            nb = tentatives.get(cle) + 1;
        }
        tentatives.put(cle, nb);

        if (nb >= MAX_TENTATIVES) {
            bloqueJusqua.put(cle, System.currentTimeMillis() + DUREE_BLOCAGE_MS);
        }
    }

    public synchronized void succes(String email) {
        String cle = email.toLowerCase();
        tentatives.remove(cle);
        bloqueJusqua.remove(cle);
    }
}
