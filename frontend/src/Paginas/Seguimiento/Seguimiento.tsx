import { type CSSProperties, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ImagenFondoSeguimiento from "../../imagenes/ImagenFondoSeguimiento.webp";
import BusquedaEnvio from "../../componentes/BusquedaEnvio/BusquedaEnvio";
import LineaSeguimiento from "../../componentes/LineaSeguimiento/LineaSeguimiento";
import { ErrorApi } from "../../Servicios/ClienteApi";
import { consultarSeguimiento, normalizarCodigo } from "../../Servicios/Seguimiento";
import type { Seguimiento as ResultadoSeguimiento } from "../../Tipos/Seguimiento";
import "./Seguimiento.css";

type Consulta =
  | { estado: "INICIAL" | "CARGANDO" }
  | { estado: "ENCONTRADO"; resultado: ResultadoSeguimiento }
  | { estado: "NO_ENCONTRADO" | "ERROR"; mensaje: string };

function IconoAdvertencia() {
  return (
    <svg
      className="seguimiento-icono-advertencia"
      viewBox="0 0 48 48"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M21.4 7.6 4.1 37.2A5 5 0 0 0 8.4 44h31.2a5 5 0 0 0 4.3-6.8L26.6 7.6a3 3 0 0 0-5.2 0Z"
        fill="currentColor"
      />
      <path
        d="M24 17v12m0 7v.1"
        stroke="var(--color-blanco)"
        strokeWidth="3.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

export default function Seguimiento() {
  const { codigo = "" } = useParams();

  return <ConsultaSeguimiento key={codigo} codigo={codigo} />;
}

function ConsultaSeguimiento({ codigo }: { codigo: string }) {
  const navegar = useNavigate();
  const [consulta, setConsulta] = useState<Consulta>({
    estado: codigo ? "CARGANDO" : "INICIAL",
  });
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    if (!codigo) return;

    let vigente = true;

    consultarSeguimiento(codigo)
      .then((resultado) => {
        if (vigente) setConsulta({ estado: "ENCONTRADO", resultado });
      })
      .catch((error: unknown) => {
        if (!vigente) return;

        setConsulta({
          estado: error instanceof ErrorApi && error.status === 404 ? "NO_ENCONTRADO" : "ERROR",
          mensaje: error instanceof Error ? error.message : "No se pudo consultar el envío.",
        });
      });

    return () => {
      vigente = false;
    };
  }, [codigo, intento]);

  function consultar(codigoIngresado: string) {
    if (codigoIngresado === normalizarCodigo(codigo)) {
      setConsulta({ estado: "CARGANDO" });
      setIntento((valor) => valor + 1);
      return;
    }

    navegar(`/seguimiento/${encodeURIComponent(codigoIngresado)}`);
  }

  const tieneResultado = consulta.estado === "ENCONTRADO";
  const consultaInicial = consulta.estado === "INICIAL";
  const clasesContenido = [
    "seguimiento-contenido",
    tieneResultado && "seguimiento-contenido--resultado",
    consultaInicial && "seguimiento-contenido--inicial",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <main
      className="seguimiento-pagina"
      style={{ "--fondo-seguimiento": `url(${ImagenFondoSeguimiento})` } as CSSProperties}
    >
      <div className={clasesContenido}>
        {consultaInicial && (
          <header className="seguimiento-presentacion">
            <h1>Seguimiento de envío</h1>
            <p>Consulta el estado actual de tu envío.</p>
          </header>
        )}

        <BusquedaEnvio
          codigoInicial={codigo}
          onBuscar={consultar}
          titulo="Buscar número de tracking"
        />

        {!consultaInicial && (
          <section className="seguimiento-panel" aria-live="polite">
            {consulta.estado === "CARGANDO" && (
              <p className="seguimiento-cargando" role="status">
                Consultando envío...
              </p>
            )}

            {(consulta.estado === "ERROR" || consulta.estado === "NO_ENCONTRADO") && (
              <div className="seguimiento-mensaje seguimiento-mensaje-error" role="alert">
                <IconoAdvertencia />
                <h2>
                  {consulta.estado === "NO_ENCONTRADO"
                    ? "Código de seguimiento no encontrado"
                    : "No pudimos consultar tu envío"}
                </h2>
                <p>{consulta.mensaje}</p>
                {consulta.estado === "ERROR" && (
                  <button
                    type="button"
                    onClick={() => {
                      setConsulta({ estado: "CARGANDO" });
                      setIntento((valor) => valor + 1);
                    }}
                  >
                    Reintentar
                  </button>
                )}
              </div>
            )}

            {consulta.estado === "ENCONTRADO" && (
              <article className="seguimiento-resultado">
                <header className="seguimiento-cabecera">
                  <div>
                    <p>Información del envío</p>
                    <h2>{consulta.resultado.codigoEnvio}</h2>
                  </div>
                  <span className="seguimiento-estado">{consulta.resultado.estadoTexto}</span>
                </header>

                <dl className="seguimiento-datos">
                  <div>
                    <dt>Origen</dt>
                    <dd>{consulta.resultado.origen}</dd>
                  </div>
                  <div>
                    <dt>Destino</dt>
                    <dd>{consulta.resultado.destino}</dd>
                  </div>
                  <div>
                    <dt>Modalidad</dt>
                    <dd>
                      {consulta.resultado.modalidad === "AGENCIA"
                        ? "Recojo en agencia"
                        : "Entrega a domicilio"}
                    </dd>
                  </div>
                </dl>

                <LineaSeguimiento
                  key={consulta.resultado.codigoEnvio}
                  seguimiento={consulta.resultado}
                />
              </article>
            )}
          </section>
        )}
      </div>
    </main>
  );
}
