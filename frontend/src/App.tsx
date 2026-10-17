import BarraNavegacion from "./componentes/BarraNavegacion/BarraNavegacion";
import Inicio from "./paginas/Inicio";
import Autenticacion from "./paginas/Autenticacion/Autenticacion";
import { Route, Routes, useLocation } from "react-router-dom";

function App() {
  const location = useLocation();
  const esAutenticacion = location.pathname === "/auth";

  return (
    <>
      {!esAutenticacion && <BarraNavegacion />}
      <Routes>
        <Route path="/auth" element={<Autenticacion />} />
        <Route path="*" element={<Inicio />} />
      </Routes>
    </>
  );
}

export default App;