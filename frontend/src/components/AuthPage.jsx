import { useState } from "react";
import { login, register } from "../api";

function AuthPage({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [nom, setNom] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setErreur("");
    setChargement(true);

    try {
      let data;
      if (mode === "login") {
        data = await login(email, password);
      } else {
        data = await register(nom, email, password);
      }
      onLogin(data);
    } catch (err) {
      setErreur(err.message);
    }
    setChargement(false);
  }

  function changerMode() {
    setErreur("");
    if (mode === "login") {
      setMode("register");
    } else {
      setMode("login");
    }
  }

  return (
    <div className="auth">
      <div className="auth-intro">
        <h1>Suivi de candidatures</h1>
        <p>Garde une trace de chaque offre, de l'envoi jusqu'à la réponse.</p>
      </div>

      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>{mode === "login" ? "Connexion" : "Créer un compte"}</h2>

        {mode === "register" && (
          <label>
            Nom
            <input value={nom} onChange={(e) => setNom(e.target.value)} required />
          </label>
        )}

        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>

        <label>
          Mot de passe
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={mode === "register" ? 8 : undefined}
            required
          />
          {mode === "register" && (
            <span className="aide">
              8 caractères minimum, avec une majuscule, une minuscule, un chiffre et un caractère spécial.
            </span>
          )}
        </label>

        {erreur && <p className="erreur">{erreur}</p>}

        <button type="submit" className="btn btn-primary" disabled={chargement}>
          {chargement ? "Patiente…" : mode === "login" ? "Se connecter" : "Créer mon compte"}
        </button>

        <button type="button" className="lien" onClick={changerMode}>
          {mode === "login" ? "Pas encore de compte ? Inscris-toi" : "Déjà un compte ? Connecte-toi"}
        </button>
      </form>
    </div>
  );
}

export default AuthPage;
