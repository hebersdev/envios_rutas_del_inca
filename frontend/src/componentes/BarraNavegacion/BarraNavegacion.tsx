import { useState } from "react";
import { Link } from "react-router-dom";
import "../BarraNavegacion/BarraNavegacion.css";
import Logo from "../../imagenes/logoRutasDelInca.png";
import SearchIcon from "../iconos/SearchIcon";
import HomeIcon from "../iconos/HomeIcon";
import MoneyIcon from "../iconos/MoneyIcon";
import StoreIcon from "../iconos/StoreIcon";

function BarraNavegacion() {
  const [menuAbierto, setMenuAbierto] = useState(false);

  const cerrarMenu = () => setMenuAbierto(false);

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
        </nav>

        <div className="barra-navegacion-auth">
          <Link to="/auth" className="barra-navegacion-boton">
            Iniciar sesión
          </Link>
        </div>
      </div>
    </header>
  );
}

export default BarraNavegacion;
