import { useState } from "react";
import type { AccionViaje } from "../../Servicios/Viajes";
import type { EstadoViaje, Viaje } from "../../Tipos/Viaje";
import DetalleViaje from "../DetalleViaje/DetalleViaje";
import "./TarjetaViaje.css";

interface Props {
  viaje: Viaje;
  sedeId: number | null;
  ocupado: boolean;
  alActualizar: (id: number, accion: AccionViaje) => void;
}

const etiquetasEstado: Record<EstadoViaje, string> = {
  PROGRAMADO: "Programado",
  EN_RUTA: "En ruta",
  LLEGADO: "En destino",
  FINALIZADO: "Finalizado",
};

function formatearFecha(fecha: string | null) {
  if (!fecha) return "Pendiente";
  return new Intl.DateTimeFormat("es-PE", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(fecha));
}

function Capacidad({ etiqueta, carga, maximo, unidad }: {
  etiqueta: string;
  carga: number;
  maximo: number;
  unidad: string;
}) {
  const porcentaje = maximo > 0 ? Math.min((carga / maximo) * 100, 100) : 0;
  const formato = new Intl.NumberFormat("es-PE", { maximumFractionDigits: 2 });

  return (
    <section className="viaje-capacidad" aria-label={`Capacidad de ${etiqueta}`}>
      <div className="viaje-capacidad-cabecera">
        <span>{etiqueta}</span>
        <strong>{formato.format(porcentaje)}%</strong>
      </div>
      <progress value={carga} max={maximo} />
      <p>{formato.format(carga)} de {formato.format(maximo)} {unidad}</p>
    </section>
  );
}

export default function TarjetaViaje({ viaje, sedeId, ocupado, alActualizar }: Props) {
  const [detalleVisible, setDetalleVisible] = useState(false);
  const esOrigen = sedeId === viaje.agenciaOrigenId;
  const esDestino = sedeId === viaje.agenciaDestinoId;

  return (
    <article className="viaje-tarjeta">
      <header className="viaje-tarjeta-cabecera">
        <div>
          <p className="viaje-identificador">Viaje #{viaje.id}</p>
          <h2>Camión {viaje.placa}</h2>
        </div>
        <span className="viaje-estado" data-estado={viaje.estado}>{etiquetasEstado[viaje.estado]}</span>
      </header>

      <section className="viaje-ruta" aria-label="Ruta del viaje">
        <div><span>Origen</span><strong>{viaje.origen}</strong></div>
        <span className="viaje-ruta-linea" aria-hidden="true" />
        <div><span>Destino</span><strong>{viaje.destino}</strong></div>
      </section>

      <dl className="viaje-resumen">
        <div><dt>Envíos</dt><dd>{viaje.cantidadEnvios}</dd></div>
        <div><dt>Salida</dt><dd>{formatearFecha(viaje.fechaSalida)}</dd></div>
        <div><dt>Llegada</dt><dd>{formatearFecha(viaje.fechaLlegada)}</dd></div>
      </dl>

      <section className="viaje-capacidades">
        <Capacidad etiqueta="Peso cargado" carga={viaje.pesoCargadoKg} maximo={viaje.pesoMaximoKg} unidad="kg" />
        <Capacidad etiqueta="Volumen ocupado" carga={viaje.volumenCargadoM} maximo={viaje.volumenMaximoM} unidad="m³" />
      </section>

      {viaje.sinMedidas && <p className="viaje-advertencia">Hay envíos sin medidas completas; el volumen final podría ser mayor.</p>}

      <footer className="viaje-acciones">
        <button type="button" className="viaje-accion-secundaria" onClick={() => setDetalleVisible((visible) => !visible)} aria-expanded={detalleVisible}>
          {detalleVisible ? "Ocultar detalle" : "Ver envíos"}
        </button>
        <div className="viaje-acciones-principales">
          {esOrigen && viaje.estado === "PROGRAMADO" && (
            <>
              <button type="button" className="viaje-accion-principal" disabled={ocupado || viaje.cantidadEnvios === 0}
                onClick={() => alActualizar(viaje.id, "despachar")}>Despachar viaje</button>
              <button type="button" className="viaje-accion-peligro" disabled={ocupado} onClick={() => {
                if (window.confirm("¿Cancelar este viaje y devolver sus envíos a pendientes?")) alActualizar(viaje.id, "cancelar");
              }}>Cancelar</button>
            </>
          )}
          {esDestino && viaje.estado === "EN_RUTA" && (
            <button type="button" className="viaje-accion-principal" disabled={ocupado} onClick={() => alActualizar(viaje.id, "llegada")}>Registrar llegada</button>
          )}
          {esDestino && viaje.estado === "LLEGADO" && (
            <button type="button" className="viaje-accion-principal" disabled={ocupado} onClick={() => alActualizar(viaje.id, "finalizar")}>Finalizar viaje</button>
          )}
        </div>
      </footer>

      {detalleVisible && <DetalleViaje id={viaje.id} />}
    </article>
  );
}
