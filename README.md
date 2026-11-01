# Suivi de candidatures

Application web pour suivre ses candidatures de stage et d'emploi : de l'envoi jusqu'à la réponse, avec des rappels de relance.

![CI](https://github.com/VOTRE-PSEUDO/suivi-candidatures/actions/workflows/ci.yml/badge.svg)

## Fonctionnalités

- Inscription et connexion sécurisées (JWT)
- Ajout, modification et suppression de candidatures
- Filtre par statut : envoyée, entretien, refusée, acceptée
- Statistiques et barre de répartition par statut
- **Relances** : une date de relance est programmée automatiquement 10 jours après l'envoi, et les entreprises à relancer s'affichent en haut du tableau de bord

## Sécurité

| Risque | Protection mise en place |
|---|---|
| Vol des mots de passe en base | Hachage BCrypt, jamais de mot de passe en clair |
| Mots de passe faibles | 8 caractères minimum avec majuscule, minuscule, chiffre et caractère spécial |
| Attaque par force brute | Compte bloqué 15 minutes après 5 échecs de connexion (HTTP 429) |
| Énumération des comptes | Même message d'erreur que l'email existe ou non |
| Accès aux données d'un autre utilisateur | Chaque requête est filtrée par l'utilisateur du token (renvoie 404) |
| Accès sans authentification | Toutes les routes sauf `/api/auth/**` exigent un token valide (renvoie 401) |
| Clickjacking, chargement de contenu | En-têtes `Content-Security-Policy`, `X-Frame-Options: DENY`, `Referrer-Policy` |
| Requêtes depuis un autre site | CORS limité à l'adresse du front |

Ces protections sont vérifiées par des tests automatisés (`SecuriteApiTest`).

## Stack

- **Backend** : Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA, PostgreSQL
- **Frontend** : React 18, Vite
- **Tests** : JUnit 5, Mockito, MockMvc, base H2 en mémoire
- **DevOps** : Docker, Docker Compose, GitHub Actions

## Architecture

```
suivi-candidatures/
├── backend/     API REST Spring Boot (controller → service → repository)
├── frontend/    Interface React
└── .github/     Intégration continue : tests et build à chaque push
```

## Lancer le projet

**1. Backend** (Docker requis)
```bash
cd backend
docker compose up --build
```
L'API est disponible sur http://localhost:8081

**2. Frontend** (Node.js requis)
```bash
cd frontend
npm install
npm run dev
```
L'application est disponible sur http://localhost:5173

**Lancer les tests sans installer Maven**
```bash
cd backend
docker run --rm -v "${PWD}:/app" -w /app maven:3.9-eclipse-temurin-17 mvn test
```

## API

| Méthode | URL | Description |
|---|---|---|
| POST | /api/auth/register | Inscription |
| POST | /api/auth/login | Connexion |
| GET | /api/candidatures | Liste (filtre optionnel `?statut=`) |
| GET | /api/candidatures/{id} | Détail |
| POST | /api/candidatures | Ajout |
| PUT | /api/candidatures/{id} | Modification |
| DELETE | /api/candidatures/{id} | Suppression |
| GET | /api/candidatures/stats | Statistiques par statut |
| GET | /api/candidatures/relances | Candidatures à relancer |
| POST | /api/candidatures/{id}/relancer | Marquer comme relancée |

Des exemples de requêtes sont dans `backend/test-api.http`.

## Configuration en production

Définir ces variables d'environnement, en particulier une clé JWT longue et secrète :

`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `FRONT_URL` côté backend, et `VITE_API_URL` côté frontend.
