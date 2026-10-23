import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.tsx";
import "./App.css";
import "./Estilos/variables.css";
import "./Estilos/selectores.css";
import { ProveedorAutenticacion } from "./Context/ProveedorAutenticacion.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ProveedorAutenticacion>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ProveedorAutenticacion>
  </StrictMode>
);