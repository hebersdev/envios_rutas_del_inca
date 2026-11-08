import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import LogoRutasDelInca from "../../imagenes/LogoRutasDelIncaColor.webp";
import { useAutenticacion } from "../../Context/UseAutenticacion";
import { cerrarSesion as solicitarCierre } from "../../Servicios/Autenticacion";
import { opcionesMenu } from "../../Configuracion/menuPorRol";
import "./MenuLateral.css";

interface MenuLateralProps {
  abierto: boolean;
  onCerrar: () => void;
}

export default function MenuLateral({ abierto, onCerrar }: MenuLateralProps) {
  const { usuario, cerrarSesion } = useAutenticacion();
  const [error, setError] = useState("");
  const [cerrando, setCerrando] = useState(false);
  const navegar = useNavigate();
  const menuVisible = opcionesMenu.filter((opcion) => (
    usuario && opcion.roles.includes(usuario.rol)
  ));

  async function manejarCerrarSesion() {
    setError("");
    setCerrando(true);
    try {
      await solicitarCierre();
      cerrarSesion();
      navegar("/auth");
    } catch (errorSolicitud) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo cerrar sesión.");
    } finally {
      setCerrando(false);
    }
  }

  return (
    <aside className={`panel-menu${abierto ? " panel-menu--abierto" : ""}`}>
      <NavLink to="/panel" className="panel-menu-marca" onClick={onCerrar}>
        <img src={LogoRutasDelInca} alt="Rutas del Inca" />
      </NavLink>
      <nav className="panel-menu-navegacion" aria-label="Menú principal">
        {menuVisible.map((opcion) => (
          <NavLink
            key={opcion.ruta}
            to={opcion.ruta}
            end={opcion.ruta === "/panel"}
            className={({ isActive }) => `panel-menu-enlace${isActive ? " panel-menu-enlace--activo" : ""}`}
            onClick={onCerrar}
          >
            {opcion.etiqueta}
          </NavLink>
        ))}
      </nav>
      <div className="panel-menu-pie">
        {error && <p className="panel-menu-error" role="alert">{error}</p>}
        <button type="button" onClick={manejarCerrarSesion} disabled={cerrando}>
          {cerrando ? "Cerrando sesión..." : "Cerrar sesión"}
        </button>
      </div>
    </aside>
  );
}
