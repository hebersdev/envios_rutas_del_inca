import { useEffect, useRef, useState, type FormEvent } from "react";
import type { Tarifa, TarifaRequest } from "../../Tipos/Tarifa";
import "./ModalTarifa.css";

interface ModalTarifaProps {
  tarifa: Tarifa | null;
  onCerrar: () => void;
  onGuardar: (datos: TarifaRequest) => Promise<void>;
}

export default function ModalTarifa({
  tarifa,
  onCerrar,
  onGuardar,
}: ModalTarifaProps) {
  const [minKg, setMinKg] = useState(String(tarifa?.rangoPesoMinKg ?? "0"));
  const [maxKg, setMaxKg] = useState(String(tarifa?.rangoPesoMaxKg ?? ""));
  const [precio, setPrecio] = useState(String(tarifa?.precioBase ?? ""));
  const [recargo, setRecargo] = useState(String(tarifa?.recargoDomicilio ?? "0"));
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);
  const primerCampo = useRef<HTMLInputElement>(null);

  useEffect(() => {
    primerCampo.current?.focus();

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
        rangoPesoMinKg: Number(minKg),
        rangoPesoMaxKg: Number(maxKg),
        precioBase: Number(precio),
        recargoDomicilio: Number(recargo || 0),
      });
    } catch (errorSolicitud: unknown) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo guardar la tarifa.");
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div
      className="tarifa-modal-fondo"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget && !guardando) onCerrar();
      }}
    >
      <section
        className="tarifa-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="tarifa-modal-titulo"
      >
        <header className="tarifa-modal-encabezado">
          <div>
            <p>Administración</p>
            <h2 id="tarifa-modal-titulo">{tarifa ? "Editar tarifa" : "Nueva tarifa"}</h2>
          </div>
          <button type="button" aria-label="Cerrar ventana" onClick={onCerrar} disabled={guardando}>
            ×
          </button>
        </header>
        <form className="tarifa-modal-formulario" onSubmit={enviar}>
          <div className="tarifa-modal-fila">
            <label>
              Peso mínimo (kg)
              <input
                type="number"
                min="0"
                step="0.01"
                ref={primerCampo}
                value={minKg}
                onChange={(evento) => setMinKg(evento.target.value)}
                required
              />
            </label>
            <label>
              Peso máximo (kg)
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={maxKg}
                onChange={(evento) => setMaxKg(evento.target.value)}
                required
              />
            </label>
          </div>
          <div className="tarifa-modal-fila">
            <label>
              Precio base (S/)
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={precio}
                onChange={(evento) => setPrecio(evento.target.value)}
                required
              />
            </label>
            <label>
              Recargo a domicilio (S/)
              <input
                type="number"
                min="0"
                step="0.01"
                value={recargo}
                onChange={(evento) => setRecargo(evento.target.value)}
              />
            </label>
          </div>
          {error && <p className="tarifa-modal-error" role="alert">{error}</p>}
          <div className="tarifa-modal-acciones">
            <button type="button" onClick={onCerrar} disabled={guardando}>Cancelar</button>
            <button type="submit" disabled={guardando}>
              {guardando ? "Guardando..." : "Guardar tarifa"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
