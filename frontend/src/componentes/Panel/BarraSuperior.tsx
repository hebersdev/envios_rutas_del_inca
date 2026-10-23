import { useEffect, useState } from "react";
import { useAutenticacion } from "../../Context/UseAutenticacion";
import { listarAgenciasActivas } from "../../Servicios/Agencias";
import "./BarraSuperior.css";

interface BarraSuperiorProps {
  onAbrirMenu: () => void;
}

export default function BarraSuperior({ onAbrirMenu }: BarraSuperiorProps) {
  const { usuario } = useAutenticacion();
  const [nombreAgencia, setNombreAgencia] = useState("");
  const [errorAgencia, setErrorAgencia] = useState("");

  useEffect(() => {
    if (!usuario?.agenciaId) return;

    let vigente = true;
    listarAgenciasActivas()
      .then((agencias) => {
        if (!vigente) return;
        const agencia = agencias.find((item) => item.id === usuario.agenciaId);
        setNombreAgencia(agencia?.nombre ?? "Sede no disponible");
      })
      .catch((errorSolicitud: unknown) => {
        if (vigente) {
          setErrorAgencia(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo cargar la sede.");
        }
      });

    return () => {
      vigente = false;
    };
  }, [usuario?.agenciaId]);

  return (
    <header className="panel-barra">
      <button
        className="panel-barra-menu"
        type="button"
        aria-label="Abrir menú"
        onClick={onAbrirMenu}
      >
        <span aria-hidden="true">☰</span>
      </button>
      <div className="panel-barra-identidad">
        <span className="panel-barra-nombre">{usuario?.nombre}</span>
        <span className="panel-barra-rol">{usuario?.rol.replaceAll("_", " ")}</span>
        {usuario?.agenciaId && (
          <span className="panel-barra-sede" title={errorAgencia || undefined}>
            {errorAgencia ? "Sede no disponible" : nombreAgencia || "Cargando sede..."}
          </span>
        )}
      </div>
    </header>
  );
}
