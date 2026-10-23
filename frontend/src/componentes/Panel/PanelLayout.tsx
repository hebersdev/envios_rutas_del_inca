import { useState } from "react";
import { Outlet } from "react-router-dom";
import MenuLateral from "./MenuLateral";
import BarraSuperior from "./BarraSuperior";
import "./PanelLayout.css";

export default function PanelLayout() {
  const [menuAbierto, setMenuAbierto] = useState(false);

  return (
    <div className="panel-layout">
      {menuAbierto && (
        <button
          className="panel-overlay"
          type="button"
          aria-label="Cerrar menú"
          onClick={() => setMenuAbierto(false)}
        />
      )}
      <MenuLateral abierto={menuAbierto} onCerrar={() => setMenuAbierto(false)} />
      <div className="panel-contenido">
        <BarraSuperior onAbrirMenu={() => setMenuAbierto(true)} />
        <main className="panel-principal">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
