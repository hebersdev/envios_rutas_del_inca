import { useEffect, useState } from "react";
import { obtenerEnvio } from "../../../Servicios/EnvioApi";
import { Link } from "react-router-dom";
import type { EnvioDetalle } from "../../../Tipos/Envio";
import { ETIQUETAS_ESTADO } from "../../../Tipos/Envio";
import "./DetalleEnvio.css";
import DescargasEnvio from "../../../componentes/DescargasEnvio/DescargasEnvio";

interface Props {
  envioId: number;
  onCerrar: () => void;
  onEstadoActualizado: () => void;
}
function formatearMoneda(valor: number): string {
  return new Intl.NumberFormat("es-PE", {
    style: "currency",
    currency: "PEN",
  }).format(valor);
}

function formatearFecha(fecha: string): string {
  return new Date(fecha).toLocaleString("es-PE", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function DetalleEnvio({
  envioId,
  onCerrar,
}: Props) {
  const [envio, setEnvio] = useState<EnvioDetalle | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [recarga, setRecarga] = useState(0);

  useEffect(() => {
    let vigente = true;

    obtenerEnvio(envioId)
      .then((data) => {
        if (vigente) setEnvio(data);
      })
      .catch((err: unknown) => {
        if (vigente) {
          setError(
            err instanceof Error ? err.message : "No se pudo cargar el envío.",
          );
        }
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
  }, [envioId, recarga]);

  function reintentar() {
    setCargando(true);
    setError("");
    setRecarga((valor) => valor + 1);
  }



  return (
    <div className="detalle-overlay" onClick={onCerrar}>
      <aside
        className="detalle-panel"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <header className="detalle-cabecera">
          <div>
            <p className="detalle-eyebrow">Envío</p>
            <h2>{envio?.codigoEnvio ?? "Cargando..."}</h2>
          </div>
          <button
            type="button"
            className="detalle-cerrar"
            onClick={onCerrar}
            aria-label="Cerrar"
          >
            ×
          </button>
        </header>

        {cargando && <p className="detalle-vacio">Cargando detalle...</p>}

        {error && (
          <div className="detalle-error" role="alert">
            <p>{error}</p>
            <button
              type="button"
              className="detalle-btn-reintentar"
              onClick={reintentar}
            >
              Reintentar
            </button>
          </div>
        )}

        {envio && (
          <div className="detalle-contenido">
            <div className="detalle-seccion">
              <h3>Estado actual</h3>
              <span
                className={`detalle-estado-badge estado-${envio.estado.toLowerCase()}`}
              >
                {ETIQUETAS_ESTADO[envio.estado]}
              </span>
            </div>

            <div className="detalle-seccion">
              <h3>Remitente</h3>
              <p>
                <strong>{envio.remitente.nombre}</strong>
              </p>
              <p>Documento: {envio.remitente.documento}</p>
              {envio.remitente.telefono && (
                <p>Teléfono: {envio.remitente.telefono}</p>
              )}
              {envio.remitente.id === null && (
                <p className="detalle-hint">Sin cuenta registrada</p>
              )}
            </div>

            <div className="detalle-seccion">
              <h3>Ruta</h3>
              <p>
                <strong>Origen:</strong> {envio.origen.nombre} —{" "}
                {envio.origen.ciudad}
              </p>
              <p>
                <strong>Destino:</strong> {envio.destino.nombre} —{" "}
                {envio.destino.ciudad}
              </p>
              <p>
                <strong>Modalidad:</strong>{" "}
                {envio.modalidad === "DOMICILIO" ? "Domicilio" : "Agencia"}
              </p>
              {envio.modalidad === "DOMICILIO" && envio.direccionEntrega && (
                <p>
                  <strong>Dirección:</strong> {envio.direccionEntrega}
                </p>
              )}
            </div>

            <div className="detalle-seccion">
              <h3>Destinatario</h3>
              <p>
                <strong>{envio.destinatarioNombre}</strong>
              </p>
              <p>Documento: {envio.destinatarioDocumento}</p>
              <p>Teléfono: {envio.destinatarioTelefono}</p>
            </div>

            <div className="detalle-seccion">
              <h3>Paquete</h3>
              <p>{envio.descripcionPaquete}</p>
              <p>Peso: {envio.pesoKg} kg</p>
              <p>
                Dimensiones: {envio.altoCm} × {envio.anchoCm} ×{" "}
                {envio.largoCm} cm
              </p>
              <p>
                <strong>Costo:</strong> {formatearMoneda(envio.costoEnvio)}
              </p>
            </div>

            <div className="detalle-seccion">
              <h3>Registro</h3>
              <p>{formatearFecha(envio.fechaRegistro)}</p>
            </div>

            <div className="detalle-acciones">
              <DescargasEnvio envioId={envio.id} codigo={envio.codigoEnvio} />

              {envio.estado !== "ENTREGADO" && (
                <p className="detalle-hint">
                  Las entregas se gestionan en <Link to="/panel/por-entregar">Por entregar</Link>.
                  Los envíos a domicilio pasan por Reparto y Mis entregas.
                </p>
              )}
            </div>
          </div>
        )}
      </aside>
    </div>
  );
}
