import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.tsx";
import "./App.css";
import "./estilos/variables.css";
import { ProveedorAutenticacion } from "./context/ProveedorAutenticacion";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ProveedorAutenticacion>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ProveedorAutenticacion>
  </StrictMode>
);