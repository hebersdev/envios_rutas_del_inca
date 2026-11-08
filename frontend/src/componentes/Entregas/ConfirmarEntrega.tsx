import { useEffect, useRef, useState, type SubmitEvent } from "react";
import { confirmarEntrega } from "../../Servicios/Entregas";
import type { Entrega } from "../../Tipos/Entrega";
import "./ConfirmarEntrega.css";

interface Props { envio: Entrega; cerrar: () => void; completada: () => void; }

export default function ConfirmarEntrega({ envio, cerrar, completada }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null);
  const guardando = useRef(false);
  const [documento, setDocumento] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => {
    const anterior = document.activeElement;
    const modal = dialogo.current;
    modal?.showModal();
    modal?.querySelector("input")?.focus();
    return () => { modal?.close(); if (anterior instanceof HTMLElement) anterior.focus(); };
  }, []);

  async function confirmar(evento: SubmitEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (guardando.current) return;
    guardando.current = true;
    setOcupado(true);
    setError("");
    try {
      await confirmarEntrega(envio.id, documento, envio.modalidad === "AGENCIA");
      completada();
    } catch (fallo: unknown) {
      setError(fallo instanceof Error ? fallo.message : "No se pudo confirmar la entrega.");
    } finally {
      guardando.current = false;
      setOcupado(false);
    }
  }

  return <dialog className="entrega-modal" ref={dialogo} aria-labelledby="entrega-titulo"
    onCancel={evento => { evento.preventDefault(); if (!guardando.current) cerrar(); }}>
    <form onSubmit={evento => void confirmar(evento)}>
      <h2 id="entrega-titulo">Confirmar entrega</h2>
      <p>Envío <strong>{envio.codigoEnvio}</strong> · {envio.destinatarioNombre}</p>
      <label htmlFor="entrega-documento">Documento de quien recibe</label>
      <input id="entrega-documento" value={documento} inputMode="numeric" pattern="[0-9]{8}|[0-9]{11}"
        maxLength={11} required disabled={ocupado} aria-describedby="entrega-ayuda entrega-error"
        onChange={evento => setDocumento(evento.target.value)} autoComplete="off" />
      <p id="entrega-ayuda">Verifica el DNI de 8 dígitos o RUC de 11 dígitos antes de confirmar.</p>
      <p id="entrega-error" className="entrega-modal-error" role="alert">{error}</p>
      <div className="entrega-modal-acciones">
        <button type="button" onClick={cerrar} disabled={ocupado}>Cancelar</button>
        <button type="submit" disabled={ocupado}>{ocupado ? "Confirmando..." : "Confirmar entrega"}</button>
      </div>
    </form>
  </dialog>;
}
