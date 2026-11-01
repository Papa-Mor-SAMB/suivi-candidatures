import { useEffect, useState } from "react";
import {
  getCandidatures,
  getStats,
  createCandidature,
  updateCandidature,
  deleteCandidature,
  getRelances,
  relancer,
} from "../api";
import { STATUTS, labelStatut } from "../statuts";
import StatsBar from "./StatsBar";
import CandidatureForm from "./CandidatureForm";
import Relances from "./Relances";

function formaterDate(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("fr-FR");
}

function relanceEnRetard(date) {
  if (!date) return false;
  return date <= new Date().toISOString().substring(0, 10);
}

function Dashboard({ nom, onLogout }) {
  const [candidatures, setCandidatures] = useState([]);
  const [stats, setStats] = useState({});
  const [relances, setRelances] = useState([]);
  const [filtre, setFiltre] = useState("");
  const [formOuvert, setFormOuvert] = useState(false);
  const [enEdition, setEnEdition] = useState(null);
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(true);

  async function charger() {
    setErreur("");
    try {
      const liste = await getCandidatures(filtre);
      const s = await getStats();
      const r = await getRelances();
      setCandidatures(liste);
      setStats(s);
      setRelances(r);
    } catch (err) {
      if (err.status === 401) {
        onLogout();
        return;
      }
      setErreur(err.message);
    }
    setChargement(false);
  }

  useEffect(() => {
    charger();
  }, [filtre]);

  function ouvrirAjout() {
    setEnEdition(null);
    setFormOuvert(true);
  }

  function ouvrirEdition(c) {
    setEnEdition(c);
    setFormOuvert(true);
  }

  async function enregistrer(form) {
    if (enEdition) {
      await updateCandidature(enEdition.id, form);
    } else {
      await createCandidature(form);
    }
    setFormOuvert(false);
    setEnEdition(null);
    charger();
  }

  async function changerStatut(c, statut) {
    try {
      await updateCandidature(c.id, { ...c, statut: statut });
      charger();
    } catch (err) {
      setErreur(err.message);
    }
  }

  async function marquerRelancee(c) {
    try {
      await relancer(c.id);
      charger();
    } catch (err) {
      setErreur(err.message);
    }
  }

  async function supprimer(c) {
    if (!window.confirm("Supprimer la candidature chez " + c.entreprise + " ?")) {
      return;
    }
    try {
      await deleteCandidature(c.id);
      charger();
    } catch (err) {
      setErreur(err.message);
    }
  }

  return (
    <div className="page">
      <header className="entete">
        <h1>Suivi de candidatures</h1>
        <div className="utilisateur">
          <span>{nom}</span>
          <button className="btn btn-discret" onClick={onLogout}>Se déconnecter</button>
        </div>
      </header>

      <Relances relances={relances} onRelancer={marquerRelancee} />

      <StatsBar stats={stats} filtre={filtre} onFiltre={setFiltre} />

      <div className="barre-outils">
        <h2>{filtre === "" ? "Toutes les candidatures" : "Candidatures : " + labelStatut(filtre)}</h2>
        <button className="btn btn-primary" onClick={ouvrirAjout}>Ajouter une candidature</button>
      </div>

      {erreur && <p className="erreur">{erreur}</p>}

      {!chargement && candidatures.length === 0 && (
        <div className="vide">
          <p>
            {filtre === ""
              ? "Aucune candidature pour l'instant. Ajoute la première pour commencer le suivi."
              : "Aucune candidature avec ce statut."}
          </p>
        </div>
      )}

      {candidatures.length > 0 && (
        <table className="tableau">
          <thead>
            <tr>
              <th>Entreprise</th>
              <th>Poste</th>
              <th>Lieu</th>
              <th>Envoyée le</th>
              <th>Relance</th>
              <th>Statut</th>
              <th><span className="sr-only">Actions</span></th>
            </tr>
          </thead>
          <tbody>
            {candidatures.map((c) => (
              <tr key={c.id}>
                <td data-label="Entreprise">
                  <strong>{c.entreprise}</strong>
                  {c.lienOffre && (
                    <a href={c.lienOffre} target="_blank" rel="noreferrer" className="offre">
                      Voir l'offre
                    </a>
                  )}
                </td>
                <td data-label="Poste">{c.poste}</td>
                <td data-label="Lieu">{c.lieu || "—"}</td>
                <td data-label="Envoyée le">{formaterDate(c.dateCandidature)}</td>
                <td data-label="Relance" className={relanceEnRetard(c.dateRelance) ? "a-relancer" : ""}>
                  {formaterDate(c.dateRelance)}
                </td>
                <td data-label="Statut">
                  <select
                    className={"pastille st-" + c.statut}
                    value={c.statut}
                    onChange={(e) => changerStatut(c, e.target.value)}
                    aria-label={"Statut de la candidature chez " + c.entreprise}
                  >
                    {STATUTS.map((s) => (
                      <option key={s.code} value={s.code}>{s.label}</option>
                    ))}
                  </select>
                </td>
                <td className="cellule-actions">
                  <button className="btn btn-discret" onClick={() => ouvrirEdition(c)}>Modifier</button>
                  <button className="btn btn-discret danger" onClick={() => supprimer(c)}>Supprimer</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {formOuvert && (
        <CandidatureForm
          candidature={enEdition}
          onSave={enregistrer}
          onCancel={() => setFormOuvert(false)}
        />
      )}
    </div>
  );
}

export default Dashboard;
