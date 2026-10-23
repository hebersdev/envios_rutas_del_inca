import BarraNavegacion from "./componentes/BarraNavegacion/BarraNavegacion";
import Inicio from "./Paginas/Inicio/Inicio";
import Autenticacion from "./Paginas/Autenticacion/Autenticacion";
import { Route, Routes, Navigate, Outlet, useLocation } from "react-router-dom";
import { useAutenticacion } from "./Context/UseAutenticacion";
import { opcionesMenu } from "./Configuracion/menuPorRol";
import PanelLayout from "./componentes/Panel/PanelLayout";
import InicioPanel from "./Paginas/Panel/InicioPanel";
import Agencias from "./Paginas/Panel/Agencias/Agencias";
import Usuarios from "./Paginas/Panel/Usuarios/Usuarios";
import Tarifas from "./Paginas/Panel/Tarifas/Tarifas";
import Camiones from "./Paginas/Panel/Camiones/Camiones";
import Cotizar from "./Paginas/Cotizar/Cotizar";

function App() {
  const location = useLocation();
  const esAutenticacion = location.pathname === "/auth";
  const esPanel = location.pathname.startsWith("/panel");

  return (
    <>
      {!esAutenticacion && !esPanel && <BarraNavegacion />}
      <Routes>
        <Route path="/auth" element={<Autenticacion />} />
        <Route element={<RutaProtegida roles={["ADMINISTRADOR", "COUNTER", "MOTORIZADO", "ENCARGADO_LOGISTICA"]} />}>
          <Route path="/panel" element={<PanelLayout />}>
            <Route index element={<InicioPanel />} />
            <Route element={<RutaProtegida roles={["ADMINISTRADOR"]} />}>
              <Route path="agencias" element={<Agencias />} />
              <Route path="usuarios" element={<Usuarios />} />
              <Route path="tarifas" element={<Tarifas />} />
            </Route>
            <Route element={<RutaProtegida roles={["ADMINISTRADOR", "ENCARGADO_LOGISTICA"]} />}>
              <Route path="camiones" element={<Camiones />} />
            </Route>
          </Route>
        </Route>
        <Route path="/cotizar" element={<Cotizar />} />
        <Route path="/inicio" element={<Inicio />} />
        <Route path="*" element={<Inicio />} />
      </Routes>
    </>
  );
}

function RutaProtegida({ roles }: { roles: string[] }) {
  const { usuario, cargando, errorCarga, reintentarCarga } = useAutenticacion();

  if (cargando) {
    return <main role="status">Verificando la sesión...</main>;
  }

  if (errorCarga) {
    return (
      <main role="alert">
        <p>No se pudo verificar la sesión: {errorCarga}</p>
        <button type="button" onClick={() => void reintentarCarga()}>
          Reintentar
        </button>
      </main>
    );
  }

  if (!usuario) {
    return <Navigate to="/auth" replace />;
  }

  if (!roles.includes(usuario.rol)) {
    const inicio = opcionesMenu.find((opcion) => opcion.roles.includes(usuario.rol));
    return <Navigate to={inicio?.ruta ?? "/inicio"} replace />;
  }

  return <Outlet />;
}

export default App;