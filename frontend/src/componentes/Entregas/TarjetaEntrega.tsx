import type { ReactNode } from "react";
import type { Entrega } from "../../Tipos/Entrega";
import { ETIQUETAS_ESTADO } from "../../Tipos/Envio";
import "./TarjetaEntrega.css";

interface Props {
  envio: Entrega;
  seleccionada?: boolean;
  seleccionar?: () => void;
  bloqueada?: boolean;
  consulta?: boolean;
  children?: ReactNode;
}

export default function TarjetaEntrega({
  envio,
  seleccionada,
  seleccionar,
  bloqueada,
  consulta,
  children,
}: Props) {
  const telefono = envio.destinatarioTelefono?.replace(/[^+0-9]/g, "");
  const fechaLlegada = envio.fechaLlegada
    ? new Date(`${envio.fechaLlegada}-05:00`)
    : null;

  return (
    <li className="entrega-tarjeta" data-consulta={consulta || undefined}>
      <div className="entrega-identidad">
        {seleccionar && (
          <label className="entrega-seleccion">
            <input
              type="checkbox"
              checked={seleccionada}
              disabled={bloqueada}
              aria-label={`Seleccionar envío ${envio.codigoEnvio}`}
              onChange={seleccionar}
            />
          </label>
        )}

        <div>
          <strong className="entrega-codigo">Código: {envio.codigoEnvio}</strong>
          <p>{envio.destinatarioNombre}</p>
        </div>

        <span className="entrega-estado">{ETIQUETAS_ESTADO[envio.estado]}</span>
      </div>

      {envio.direccionEntrega && (
        <p className="entrega-direccion">
          <strong>Dirección:</strong> {envio.direccionEntrega}
        </p>
      )}

      <p>Sede destino: {envio.sedeDestino}</p>

      {fechaLlegada && (
        <p>
          <span>Llegó: </span>
          <time dateTime={`${envio.fechaLlegada}-05:00`}>
            {fechaLlegada.toLocaleString("es-PE", { timeZone: "America/Lima" })}
          </time>
        </p>
      )}

      {envio.modalidad === "DOMICILIO" && (
        <p>Motorizado: {envio.motorizadoNombre ?? "Sin asignar"}</p>
      )}

      <div className="entrega-acciones">
        {telefono && (
          <a className="entrega-telefono" href={`tel:${telefono}`}>
            Llamar: {envio.destinatarioTelefono}
          </a>
        )}
        {consulta && <p>Lo reparte un motorizado</p>}
        {children}
      </div>
    </li>
  );
}
