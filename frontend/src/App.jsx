import { useState } from "react";
import AuthPage from "./components/AuthPage";
import Dashboard from "./components/Dashboard";

function App() {
  const [nom, setNom] = useState(localStorage.getItem("nom"));
  const [connecte, setConnecte] = useState(localStorage.getItem("token") !== null);

  function handleLogin(data) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("nom", data.nom);
    setNom(data.nom);
    setConnecte(true);
  }

  function handleLogout() {
    localStorage.removeItem("token");
    localStorage.removeItem("nom");
    setNom(null);
    setConnecte(false);
  }

  if (!connecte) {
    return <AuthPage onLogin={handleLogin} />;
  }
  return <Dashboard nom={nom} onLogout={handleLogout} />;
}

export default App;
