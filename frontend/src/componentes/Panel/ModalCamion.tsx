import { useEffect, useRef, useState, type FormEvent } from "react";
import type { AgenciaResumen } from "../../Tipos/Agencia";
import "./ModalCamion.css";
import type { Camion, CamionRequest } from "../../Tipos/Camion";

interface ModalCamionProps {
  agencias: AgenciaResumen[];
  camion: Camion | null;
  onCerrar: () => void;
  onGuardar: (datos: CamionRequest) => Promise<void>;
}

export default function ModalCamion({
  agencias,
  camion,
  onCerrar,
  onGuardar,
}: ModalCamionProps) {
  const [placa, setPlaca] = useState(camion?.placa ?? "");
  const [pesoMaximoKg, setPesoMaximoKg] = useState(String(camion?.pesoMaximoKg ?? ""));
  const [volumenMaximoM3, setVolumenMaximoM3] = useState(String(camion?.volumenMaximoM3 ?? ""));
  const [agenciaBaseId, setAgenciaBaseId] = useState(String(camion?.agenciaBaseId ?? ""));
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);
  const campoPlaca = useRef<HTMLInputElement>(null);

  useEffect(() => {
    campoPlaca.current?.focus();
    function manejarEscape(evento: KeyboardEvent) {
      if (evento.key === "Escape" && !guardando) onCerrar();
    }
    window.addEventListener("keydown", manejarEscape);
    return () => window.removeEventListener("keydown", manejarEscape);
  }, [guardando, onCerrar]);

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError("");
    setGuardando(true);
    try {
      await onGuardar({
        placa: placa.trim().toUpperCase(),
        pesoMaximoKg: Number(pesoMaximoKg),
        volumenMaximoM3: Number(volumenMaximoM3),
        agenciaBaseId: Number(agenciaBaseId),
      });
    } catch (errorSolicitud: unknown) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo guardar el camión.");
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div
      className="camion-modal-fondo"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget && !guardando) onCerrar();
      }}
    >
      <section className="camion-modal" role="dialog" aria-modal="true" aria-labelledby="camion-modal-titulo">
        <header className="camion-modal-encabezado">
          <div>
            <p>Flota</p>
            <h2 id="camion-modal-titulo">{camion ? "Editar camión" : "Nuevo camión"}</h2>
          </div>
          <button type="button" aria-label="Cerrar ventana" onClick={onCerrar} disabled={guardando}>×</button>
        </header>
        <form className="camion-modal-formulario" onSubmit={enviar}>
          <label>
            Placa
            <input
              ref={campoPlaca}
              value={placa}
              onChange={(evento) => setPlaca(evento.target.value)}
              maxLength={15}
              pattern="[A-Za-z0-9-]+"
              required
            />
          </label>
          <div className="camion-modal-fila">
            <label>
              Peso máximo (kg)
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={pesoMaximoKg}
                onChange={(evento) => setPesoMaximoKg(evento.target.value)}
                required
              />
            </label>
            <label>
              Volumen máximo (m³)
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={volumenMaximoM3}
                onChange={(evento) => setVolumenMaximoM3(evento.target.value)}
                required
              />
            </label>
          </div>
          <label>
            Sede base
            <select
              value={agenciaBaseId}
              onChange={(evento) => setAgenciaBaseId(evento.target.value)}
              required
            >
              <option value="">Selecciona sede base</option>
              {agencias.map((agencia) => (
                <option key={agencia.id} value={agencia.id}>
                  {agencia.nombre} — {agencia.ciudad}
                </option>
              ))}
            </select>
          </label>
          {error && <p className="camion-modal-error" role="alert">{error}</p>}
          <div className="camion-modal-acciones">
            <button type="button" onClick={onCerrar} disabled={guardando}>Cancelar</button>
            <button type="submit" disabled={guardando}>
              {guardando ? "Guardando..." : "Guardar camión"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
