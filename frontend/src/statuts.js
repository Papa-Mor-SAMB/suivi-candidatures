export const STATUTS = [
  { code: "ENVOYEE", label: "Envoyée" },
  { code: "ENTRETIEN", label: "Entretien" },
  { code: "REFUSEE", label: "Refusée" },
  { code: "ACCEPTEE", label: "Acceptée" },
];

export function labelStatut(code) {
  for (let i = 0; i < STATUTS.length; i++) {
    if (STATUTS[i].code === code) {
      return STATUTS[i].label;
    }
  }
  return code;
}
