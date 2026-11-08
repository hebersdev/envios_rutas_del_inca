import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import "../BarraNavegacion/BarraNavegacion.css";
import Logo from "../../imagenes/logoRutasDelInca.webp";
import SearchIcon from "../Iconos/SearchIcon";
import HomeIcon from "../Iconos/HomeIcon";
import MoneyIcon from "../Iconos/MoneyIcon";
import StoreIcon from "../Iconos/StoreIcon";
import { useAutenticacion } from "../../Context/UseAutenticacion";
import { cerrarSesion as solicitarCierreSesion } from "../../Servicios/Autenticacion";

function BarraNavegacion() {
  const [perfilAbierto, setPerfilAbierto] = useState(false);
  const [errorSesion, setErrorSesion] = useState("");
  const perfilReferencia = useRef<HTMLDivElement>(null);
  const cierrePerfilPendiente = useRef<ReturnType<typeof setTimeout> | null>(null);
  const { usuario, cerrarSesion } = useAutenticacion();

  const cancelarCierrePerfil = () => {
    if (cierrePerfilPendiente.current) clearTimeout(cierrePerfilPendiente.current);
    cierrePerfilPendiente.current = null;
  };
  const cerrarPerfil = () => {
    cancelarCierrePerfil();
    setPerfilAbierto(false);
  };
  const programarCierrePerfil = () => {
    cancelarCierrePerfil();
    cierrePerfilPendiente.current = setTimeout(cerrarPerfil, 2000);
  };
  const primerNombre = usuario?.nombre.trim().split(/\s+/)[0] ?? "Perfil";

  useEffect(() => {
    function cerrarConEscape(evento: KeyboardEvent) {
      if (evento.key === "Escape") {
        if (cierrePerfilPendiente.current) clearTimeout(cierrePerfilPendiente.current);
        cierrePerfilPendiente.current = null;
        setPerfilAbierto(false);
      }
    }

    window.addEventListener("keydown", cerrarConEscape);
    return () => window.removeEventListener("keydown", cerrarConEscape);
  }, []);

  useEffect(() => () => cancelarCierrePerfil(), []);

  useEffect(() => {
    function cerrarAlPresionarFuera(evento: PointerEvent) {
      if (!perfilReferencia.current?.contains(evento.target as Node)) {
        if (cierrePerfilPendiente.current) clearTimeout(cierrePerfilPendiente.current);
        cierrePerfilPendiente.current = null;
        setPerfilAbierto(false);
      }
    }

    document.addEventListener("pointerdown", cerrarAlPresionarFuera);
    return () => document.removeEventListener("pointerdown", cerrarAlPresionarFuera);
  }, []);

  async function salir() {
    setErrorSesion("");

    try {
      await solicitarCierreSesion();
      cerrarSesion();
      cerrarPerfil();
    } catch (error) {
      setErrorSesion(error instanceof Error ? error.message : "No se pudo cerrar la sesión.");
    }
  }

  return (
    <header className="encabezado-navegacion">
      <div className="barra-navegacion">
        <Link className="barra-navegacion-logo-enlace" to="/inicio" aria-label="Rutas del Inca, inicio">
          <img src={Logo} className="barra-navegacion-logo" alt="Rutas del Inca" />
        </Link>

        <nav
          id="menu-navegacion"
          className="barra-navegacion-enlaces"
          aria-label="Navegación principal"
        >
          <Link to="/inicio"><HomeIcon /><span>Inicio</span></Link>
          <Link to="/seguimiento"><SearchIcon /><span>Rastrea</span></Link>
          <Link to="/cotizar"><MoneyIcon /><span>Tarifas</span></Link>
          <Link to="/agencias"><StoreIcon /><span>Agencias</span></Link>
          {usuario?.rol === "CLIENTE" && <Link className="barra-navegacion-enlace-mis-envios" to="/mis-envios" aria-label="Mis envíos">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true">
              <path d="m3 7 9-4 9 4v10l-9 4-9-4V7Zm0 0 9 4 9-4M12 11v10M7 5l9 4" />
            </svg><span>Mis envíos</span></Link>}
        </nav>

        <div
          ref={perfilReferencia}
          className="barra-navegacion-perfil"
          onMouseEnter={() => {
            cancelarCierrePerfil();
            setPerfilAbierto(true);
          }}
          onMouseLeave={programarCierrePerfil}
        >
          <button
            className={`barra-navegacion-perfil-disparador${usuario ? " barra-navegacion-perfil-disparador--autenticado" : ""}`}
            type="button"
            aria-label={usuario ? `Perfil de ${primerNombre}` : "Iniciar sesión"}
            aria-expanded={perfilAbierto}
            aria-controls="menu-perfil"
            onClick={() => setPerfilAbierto((abierto) => !abierto)}
          >
            <svg viewBox="0 0 24 24" fill="none" strokeWidth="1.5" stroke="currentColor" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" />
            </svg>
            <span>{usuario ? primerNombre : "Perfil"}</span>
          </button>

          {perfilAbierto && (
            <div id="menu-perfil" className="barra-navegacion-perfil-menu">
              <p className="barra-navegacion-perfil-titulo">Perfil</p>
              {usuario ? (
                <button className="barra-navegacion-perfil-opcion" type="button" onClick={salir}>
                  Cerrar sesión
                </button>
              ) : (
                <Link to="/auth" className="barra-navegacion-perfil-opcion" onClick={cerrarPerfil}>
                  Iniciar sesión o regístrate
                </Link>
              )}
              <a className="barra-navegacion-perfil-opcion" href="mailto:soporte@rutasdelinca.com" onClick={cerrarPerfil}>
                Ayuda
              </a>
              {errorSesion && <span className="barra-navegacion-perfil-error" role="alert">{errorSesion}</span>}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default BarraNavegacion;
