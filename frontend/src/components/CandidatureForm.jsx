import { useState } from "react";
import { STATUTS } from "../statuts";

function CandidatureForm({ candidature, onSave, onCancel }) {
  const aujourdhui = new Date().toISOString().substring(0, 10);

  const [form, setForm] = useState({
    entreprise: candidature ? candidature.entreprise : "",
    poste: candidature ? candidature.poste : "",
    lieu: candidature ? candidature.lieu || "" : "",
    dateCandidature: candidature ? candidature.dateCandidature || aujourdhui : aujourdhui,
    dateRelance: candidature ? candidature.dateRelance || "" : "",
    statut: candidature ? candidature.statut : "ENVOYEE",
    lienOffre: candidature ? candidature.lienOffre || "" : "",
    notes: candidature ? candidature.notes || "" : "",
  });
  const [erreur, setErreur] = useState("");

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setErreur("");
    const donnees = { ...form };
    if (donnees.dateRelance === "") {
      donnees.dateRelance = null;
    }
    try {
      await onSave(donnees);
    } catch (err) {
      setErreur(err.message);
    }
  }

  return (
    <div className="modal-fond" onClick={onCancel}>
      <form className="modal" onSubmit={handleSubmit} onClick={(e) => e.stopPropagation()}>
        <h2>{candidature ? "Modifier la candidature" : "Nouvelle candidature"}</h2>

        <div className="grille">
          <label>
            Entreprise
            <input name="entreprise" value={form.entreprise} onChange={handleChange} required />
          </label>
          <label>
            Poste
            <input name="poste" value={form.poste} onChange={handleChange} required />
          </label>
          <label>
            Lieu
            <input name="lieu" value={form.lieu} onChange={handleChange} />
          </label>
          <label>
            Date de candidature
            <input type="date" name="dateCandidature" value={form.dateCandidature} onChange={handleChange} />
          </label>
          <label>
            Statut
            <select name="statut" value={form.statut} onChange={handleChange}>
              {STATUTS.map((s) => (
                <option key={s.code} value={s.code}>{s.label}</option>
              ))}
            </select>
          </label>
          <label>
            Date de relance
            <input type="date" name="dateRelance" value={form.dateRelance} onChange={handleChange} />
            {!candidature && (
              <span className="aide">Laisse vide : relance prévue 10 jours après l'envoi.</span>
            )}
          </label>
          <label>
            Lien de l'offre
            <input type="url" name="lienOffre" value={form.lienOffre} onChange={handleChange} placeholder="https://" />
          </label>
        </div>

        <label>
          Notes
          <textarea name="notes" rows="3" value={form.notes} onChange={handleChange} />
        </label>

        {erreur && <p className="erreur">{erreur}</p>}

        <div className="actions">
          <button type="button" className="btn" onClick={onCancel}>Annuler</button>
          <button type="submit" className="btn btn-primary">
            {candidature ? "Enregistrer" : "Ajouter"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CandidatureForm;
