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
import Envios from "./Paginas/Panel/Envios/Envios";
import Seguimiento from "./Paginas/Seguimiento/Seguimiento";
import Viajes from "./Paginas/Panel/Viajes/Viajes";
import PorEntregar from "./Paginas/Panel/PorEntregar/PorEntregar";
import Reparto from "./Paginas/Panel/Reparto/Reparto";
import MisEntregas from "./Paginas/Panel/MisEntregas/MisEntregas";
import MisEnvios from "./Paginas/MisEnvios/MisEnvios";
import Reportes from "./Paginas/Panel/Reportes/Reportes";

function App() {
  const location = useLocation();
  const esAutenticacion = location.pathname === "/auth";
  const esPanel = location.pathname.startsWith("/panel");

  return (
    <>
      {!esAutenticacion && !esPanel && <BarraNavegacion />}
      <Routes>
        <Route path="/auth" element={<Autenticacion />} />
        <Route element={<RutaProtegida roles={["CLIENTE"]} />}>
          <Route path="/mis-envios" element={<MisEnvios />} />
        </Route>

        <Route
          element={
            <RutaProtegida
              roles={[
                "ADMINISTRADOR",
                "COUNTER",
                "MOTORIZADO",
                "ENCARGADO_LOGISTICA",
              ]}
            />
          }
        >
          <Route path="/panel" element={<PanelLayout />}>
            <Route index element={<InicioPanel />} />

            <Route
              element={<RutaProtegida roles={["COUNTER"]} />}>
              <Route path="envios" element={<Envios />} />
            </Route>

            <Route element={<RutaProtegida roles={["ADMINISTRADOR"]} />}>
              <Route path="agencias" element={<Agencias />} />
              <Route path="usuarios" element={<Usuarios />} />
              <Route path="tarifas" element={<Tarifas />} />
            </Route>

            <Route element={<RutaProtegida roles={["COUNTER"]} />}>
              <Route path="por-entregar" element={<PorEntregar />} />
            </Route>
            <Route element={<RutaProtegida roles={["ENCARGADO_LOGISTICA"]} />}>
              <Route path="reparto" element={<Reparto />} />
            </Route>
            <Route element={<RutaProtegida roles={["MOTORIZADO"]} />}>
              <Route path="mis-entregas" element={<MisEntregas />} />
            </Route>

            <Route
              element={
                <RutaProtegida
                  roles={["ADMINISTRADOR", "ENCARGADO_LOGISTICA"]}
                />
              }
            >
              <Route path="camiones" element={<Camiones />} />
            </Route>
            <Route element={<RutaProtegida roles={["ENCARGADO_LOGISTICA"]} />}>
              <Route path="viajes" element={<Viajes />} />
              <Route path="reportes" element={<Reportes />} />
              <Route path="pendientes" element={<Viajes key="PENDIENTES" vista="PENDIENTES" />} />
              <Route path="llegadas" element={<Viajes key="LLEGADAS" vista="LLEGADAS" />} />
            </Route>
          </Route>
        </Route>

        <Route path="/cotizar" element={<Cotizar />} />
        <Route path="/seguimiento" element={<Seguimiento />} />
        <Route path="/seguimiento/:codigo" element={<Seguimiento />} />
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
    const inicio = opcionesMenu.find((opcion) =>
      opcion.roles.includes(usuario.rol),
    );
    return <Navigate to={inicio?.ruta ?? "/inicio"} replace />;
  }

  return <Outlet />;
}

export default App;
