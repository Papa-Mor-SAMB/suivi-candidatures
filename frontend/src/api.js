const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

async function request(path, method = "GET", body = null) {
  const headers = { "Content-Type": "application/json" };

  const token = localStorage.getItem("token");
  if (token) {
    headers["Authorization"] = "Bearer " + token;
  }

  const options = { method: method, headers: headers };
  if (body) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(API_URL + path, options);

  if (!response.ok) {
    let message = "Erreur " + response.status;
    try {
      const data = await response.json();
      if (data.message) {
        message = data.message;
      }
    } catch (e) {
      // pas de corps JSON
    }
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export function register(nom, email, password) {
  return request("/api/auth/register", "POST", { nom, email, password });
}

export function login(email, password) {
  return request("/api/auth/login", "POST", { email, password });
}

export function getCandidatures(statut) {
  let url = "/api/candidatures";
  if (statut) {
    url = url + "?statut=" + statut;
  }
  return request(url);
}

export function getStats() {
  return request("/api/candidatures/stats");
}

export function getRelances() {
  return request("/api/candidatures/relances");
}

export function relancer(id) {
  return request("/api/candidatures/" + id + "/relancer", "POST");
}

export function createCandidature(candidature) {
  return request("/api/candidatures", "POST", candidature);
}

export function updateCandidature(id, candidature) {
  return request("/api/candidatures/" + id, "PUT", candidature);
}

export function deleteCandidature(id) {
  return request("/api/candidatures/" + id, "DELETE");
}
