import { STATUTS } from "../statuts";

function StatsBar({ stats, filtre, onFiltre }) {
  const total = stats.TOTAL || 0;

  return (
    <section className="stats">
      <div className="barre" aria-hidden="true">
        {total > 0 &&
          STATUTS.map((s) => {
            const nb = stats[s.code] || 0;
            if (nb === 0) return null;
            return (
              <span
                key={s.code}
                className={"segment st-" + s.code}
                style={{ flexGrow: nb }}
              />
            );
          })}
      </div>

      <div className="filtres">
        <button
          className={"filtre" + (filtre === "" ? " actif" : "")}
          onClick={() => onFiltre("")}
        >
          <strong>{total}</strong>
          <span>Toutes</span>
        </button>

        {STATUTS.map((s) => (
          <button
            key={s.code}
            className={"filtre f-" + s.code + (filtre === s.code ? " actif" : "")}
            onClick={() => onFiltre(s.code)}
          >
            <strong>{stats[s.code] || 0}</strong>
            <span>{s.label}</span>
          </button>
        ))}
      </div>
    </section>
  );
}

export default StatsBar;
