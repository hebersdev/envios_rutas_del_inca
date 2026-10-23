import { useEffect, useRef, useState, type FormEvent } from "react";
import type { Agencia, AgenciaRequest } from "../../Tipos/Agencia";
import "./ModalAgencia.css";

interface ModalAgenciaProps {
  abierta: boolean;
  agencia: Agencia | null;
  onCerrar: () => void;
  onGuardar: (datos: AgenciaRequest) => Promise<void>;
}

const formularioVacio: AgenciaRequest = {
  nombre: "",
  direccion: "",
  ciudad: "",
  departamento: "",
};

export default function ModalAgencia({
  abierta,
  agencia,
  onCerrar,
  onGuardar,
}: ModalAgenciaProps) {
  const [formulario, setFormulario] = useState<AgenciaRequest>(() => agencia ? {
    nombre: agencia.nombre,
    direccion: agencia.direccion,
    ciudad: agencia.ciudad,
    departamento: agencia.departamento,
  } : formularioVacio);
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);
  const primerCampo = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!abierta) return;
    primerCampo.current?.focus();

    function manejarEscape(evento: KeyboardEvent) {
      if (evento.key === "Escape" && !guardando) onCerrar();
    }

    window.addEventListener("keydown", manejarEscape);
    return () => window.removeEventListener("keydown", manejarEscape);
  }, [abierta, agencia, guardando, onCerrar]);

  if (!abierta) return null;

  function actualizar(campo: keyof AgenciaRequest, valor: string) {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
  }

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError("");
    setGuardando(true);
    try {
      await onGuardar({
        nombre: formulario.nombre.trim(),
        direccion: formulario.direccion.trim(),
        ciudad: formulario.ciudad.trim(),
        departamento: formulario.departamento.trim(),
      });
    } catch (errorSolicitud) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo guardar la sede.");
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div
      className="agencia-modal-fondo"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget && !guardando) onCerrar();
      }}
    >
      <section
        className="agencia-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="agencia-modal-titulo"
      >
        <div className="agencia-modal-encabezado">
          <div>
            <p className="agencia-modal-eyebrow">Administración</p>
            <h2 id="agencia-modal-titulo">{agencia ? "Editar sede" : "Nueva sede"}</h2>
          </div>
          <button
            className="agencia-modal-cerrar"
            type="button"
            aria-label="Cerrar ventana"
            onClick={onCerrar}
            disabled={guardando}
          >
            ×
          </button>
        </div>
        <form className="agencia-modal-formulario" onSubmit={enviar}>
          <label>
            Nombre
            <input
              ref={primerCampo}
              value={formulario.nombre}
              onChange={(evento) => actualizar("nombre", evento.target.value)}
              maxLength={150}
              required
            />
          </label>
          <label>
            Dirección
            <input
              value={formulario.direccion}
              onChange={(evento) => actualizar("direccion", evento.target.value)}
              maxLength={255}
              required
            />
          </label>
          <label>
            Ciudad
            <input
              value={formulario.ciudad}
              onChange={(evento) => actualizar("ciudad", evento.target.value)}
              maxLength={100}
              required
            />
          </label>
          <label>
            Departamento
            <input
              value={formulario.departamento}
              onChange={(evento) => actualizar("departamento", evento.target.value)}
              maxLength={100}
              required
            />
          </label>
          {error && <p className="agencia-modal-error" role="alert">{error}</p>}
          <div className="agencia-modal-acciones">
            <button type="button" className="agencia-boton-secundario" onClick={onCerrar} disabled={guardando}>
              Cancelar
            </button>
            <button type="submit" className="agencia-boton-principal" disabled={guardando}>
              {guardando ? "Guardando..." : "Guardar sede"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
