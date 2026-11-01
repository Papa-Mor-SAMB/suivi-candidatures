function formaterDate(date) {
  return new Date(date).toLocaleDateString("fr-FR");
}

function joursDeRetard(date) {
  const aujourdhui = new Date();
  aujourdhui.setHours(0, 0, 0, 0);
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return Math.round((aujourdhui - d) / 86400000);
}

function Relances({ relances, onRelancer }) {
  if (relances.length === 0) {
    return null;
  }

  return (
    <section className="relances" aria-labelledby="titre-relances">
      <h2 id="titre-relances">
        {relances.length === 1
          ? "1 entreprise à relancer"
          : relances.length + " entreprises à relancer"}
      </h2>
      <ul>
        {relances.map((c) => {
          const retard = joursDeRetard(c.dateRelance);
          return (
            <li key={c.id}>
              <div>
                <strong>{c.entreprise}</strong> · {c.poste}
                <span className="quand">
                  {retard === 0
                    ? "Aujourd'hui"
                    : "Prévue le " + formaterDate(c.dateRelance) + " (" + retard + " j de retard)"}
                </span>
              </div>
              <div className="relance-actions">
                {c.lienOffre && (
                  <a href={c.lienOffre} target="_blank" rel="noreferrer">Voir l'offre</a>
                )}
                <button className="btn" onClick={() => onRelancer(c)}>J'ai relancé</button>
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}

export default Relances;
