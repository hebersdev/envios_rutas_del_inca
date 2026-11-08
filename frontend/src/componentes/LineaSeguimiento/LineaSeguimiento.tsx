import type { EstadoEnvio } from "../../Tipos/Envio";
import { TEXTOS_SEGUIMIENTO, type Seguimiento } from "../../Tipos/Seguimiento";
import "./LineaSeguimiento.css";

const etapas: { titulo: string; estados: EstadoEnvio[] }[] = [
  { titulo: "Recepcionado", estados: ["REGISTRADO"] },
  {
    titulo: "En proceso de envío",
    estados: ["ASIGNADO_A_VIAJE", "EN_RUTA", "LLEGO_A_AGENCIA_DESTINO"],
  },
  {
    titulo: "En proceso de entrega",
    estados: ["LISTO_PARA_RECOJO", "EN_REPARTO", "ENTREGADO"],
  },
];

const fechaPeruana = new Intl.DateTimeFormat("es-PE", {
  timeZone: "America/Lima",
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
});

const horaPeruana = new Intl.DateTimeFormat("es-PE", {
  timeZone: "America/Lima",
  hour: "numeric",
  minute: "2-digit",
  hour12: true,
});

function IconoEtapa({ etapa }: { etapa: number }) {
  return (
    <svg
      viewBox="0 0 64 64"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {etapa === 0 ? (
        <>
          <rect x="10" y="8" width="44" height="34" rx="3" />
          <path d="M10 16h44M26 42v8m12-8v8M20 50h24M17 12h1m5 0h1" />
          <path d="m24 27 8-4 8 4v9l-8 4-8-4zm0 0 8 4 8-4m-8 4v9" />
        </>
      ) : etapa === 1 ? (
        <>
          <path d="M5 17h31v25H5zm31 8h12l10 11v6H36M42 25v11h16" />
          <circle cx="16" cy="44" r="5" />
          <circle cx="47" cy="44" r="5" />
          <path d="M21 44h21M10 10h18m-18 5h12M15 30h12m-4-4 4 4-4 4" />
        </>
      ) : (
        <>
          <path d="m19 22 13-7 13 7v16l-13 7-13-7zm0 0 13 7 13-7m-13 7v16" />
          <path d="M7 42h8l7 7h20l10-9a4 4 0 0 1 5 6L44 57H23L12 50H7zm19 7h12" />
          <circle cx="48" cy="12" r="9" />
          <path d="m44 12 3 3 5-6" />
        </>
      )}
    </svg>
  );
}

export default function LineaSeguimiento({ seguimiento }: { seguimiento: Seguimiento }) {
  const actual = etapas.findIndex((etapa) => etapa.estados.includes(seguimiento.estado));

  return (
    <ol className="seguimiento-etapas" aria-label="Recorrido del envío">
      {etapas.map((etapa, indice) => {
        const alcanzada = indice <= actual;
        const eventos = seguimiento.historial
          .filter((evento) => etapa.estados.includes(evento.estado))
          .toReversed();

        return (
          <li
            key={etapa.titulo}
            className="seguimiento-etapa"
            data-alcanzada={alcanzada}
            data-actual={indice === actual}
            aria-current={indice === actual ? "step" : undefined}
          >
            <h3>{etapa.titulo}</h3>
            <div className="seguimiento-icono">
              <IconoEtapa etapa={indice} />
            </div>
            <p className="seguimiento-resumen">
              {alcanzada
                ? indice === 0
                  ? "Registro de envío exitoso"
                  : indice === actual
                    ? seguimiento.estadoTexto
                    : "Etapa completada"
                : "Pendiente"}
            </p>
            <h4 className="seguimiento-detalle">Detalle</h4>

            {eventos.length > 0 ? (
              <ol className="seguimiento-eventos" aria-label={`Detalle: ${etapa.titulo}`}>
                {eventos.map((evento, posicion) => {
                  const fecha = new Date(`${evento.fecha}-05:00`);

                  return (
                    <li key={`${evento.fecha}-${evento.estado}-${posicion}`}>
                      <time dateTime={`${evento.fecha}-05:00`}>
                        {fechaPeruana.format(fecha)} <span>{horaPeruana.format(fecha)}</span>
                      </time>
                      <strong>{TEXTOS_SEGUIMIENTO[evento.estado]}</strong>
                      {evento.sede && <p>{evento.sede}</p>}
                    </li>
                  );
                })}
              </ol>
            ) : (
              <p className="seguimiento-sineventos">
                No hay movimientos registrados en esta etapa.
              </p>
            )}
          </li>
        );
      })}
    </ol>
  );
}
