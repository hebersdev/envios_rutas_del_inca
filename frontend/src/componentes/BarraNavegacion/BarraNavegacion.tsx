import { useState } from "react";
import { Link } from "react-router-dom";
import "../BarraNavegacion/BarraNavegacion.css";
import Logo from "../../imagenes/logoRutasDelInca.png";
import SearchIcon from "../iconos/SearchIcon";
import HomeIcon from "../iconos/HomeIcon";
import MoneyIcon from "../iconos/MoneyIcon";
import StoreIcon from "../iconos/StoreIcon";
import { useAutenticacion } from "../../context/UseAutenticacion";
import { cerrarSesion as solicitarCierreSesion } from "../../servicios/autenticacion";

function BarraNavegacion() {
  const [menuAbierto, setMenuAbierto] = useState(false);
  const [errorSesion, setErrorSesion] = useState("");
  const { usuario, cerrarSesion } = useAutenticacion();

  const cerrarMenu = () => setMenuAbierto(false);

  async function salir() {
    setErrorSesion("");

    try {
      await solicitarCierreSesion();
      cerrarSesion();
    } catch (error) {
      setErrorSesion(error instanceof Error ? error.message : "No se pudo cerrar la sesión.");
    }
  }

  return (
    <header className="encabezado-navegacion">
      <div className="barra-navegacion">
        <img src={Logo} className="barra-navegacion-logo" alt="Rutas del Inca"/>

        <button
          className="barra-navegacion-menu"
          type="button"
          aria-label={menuAbierto ? "Cerrar menú de navegación" : "Abrir menú de navegación"}
          aria-expanded={menuAbierto}
          aria-controls="menu-navegacion"
          onClick={() => setMenuAbierto((abierto) => !abierto)}
        >
          <span />
          <span />
          <span />
        </button>

        <nav
          id="menu-navegacion"
          className={`barra-navegacion-enlaces${menuAbierto ? " barra-navegacion-enlaces--abierto" : ""}`}
          aria-label="Navegación principal"
        >
          <Link to="/inicio" onClick={cerrarMenu}><HomeIcon />Inicio</Link>
          <Link to="/consulta" onClick={cerrarMenu}><SearchIcon />Rastrea</Link>
          <Link to="/cotizar" onClick={cerrarMenu}><MoneyIcon />Tarifas</Link>
          <Link to="/agencias" onClick={cerrarMenu}><StoreIcon />Agencias</Link>
          <div className="barra-navegacion-auth">
            {usuario ? (
              <button className="barra-navegacion-boton" type="button" onClick={salir}>
                Cerrar sesión
              </button>
            ) : (
              <Link to="/auth" className="barra-navegacion-boton" onClick={cerrarMenu}>
                Iniciar sesión
              </Link>
            )}
            {errorSesion && <span role="alert">{errorSesion}</span>}
          </div>
        </nav>
      </div>
    </header>
  );
}

export default BarraNavegacion;
