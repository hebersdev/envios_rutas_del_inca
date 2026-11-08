import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAutenticacion } from "../../Context/UseAutenticacion";
import { listarMisEnvios } from "../../Servicios/MisEnvios";
import { ETIQUETAS_ESTADO } from "../../Tipos/Envio";
import type { EnvioCliente, PaginaEnviosCliente } from "../../Tipos/EnvioCliente";
import "./MisEnvios.css";

const fecha = (valor: string) => new Date(`${valor}-05:00`).toLocaleString("es-PE", {
  timeZone: "America/Lima",
  day: "2-digit",
  month: "short",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

const moneda = new Intl.NumberFormat("es-PE", {
  style: "currency",
  currency: "PEN",
});

interface Resultado {
  clave: string;
  datos: PaginaEnviosCliente | null;
  error: string;
}

function useListaEnvios(
  correo: string | undefined,
  anteriores: boolean,
  pagina: number,
  activa = true,
) {
  const [resultado, setResultado] = useState<Resultado | null>(null);
  const clave = `${correo}-${anteriores}-${pagina}`;

  useEffect(() => {
    if (!activa) return;

    let vigente = true;
    listarMisEnvios(pagina, anteriores)
      .then((datos) => {
        if (vigente) setResultado({ clave, datos, error: "" });
      })
      .catch((error: unknown) => {
        if (vigente) {
          setResultado({
            clave,
            datos: null,
            error: error instanceof Error ? error.message : "No se pudieron cargar tus envíos.",
          });
        }
      });

    return () => { vigente = false; };
  }, [activa, anteriores, clave, pagina]);

  return {
    cargando: activa && resultado?.clave !== clave,
    datos: resultado?.clave === clave ? resultado.datos : null,
    error: resultado?.clave === clave ? resultado.error : "",
  };
}

function TarjetaEnvio({ envio, actual }: { envio: EnvioCliente; actual: boolean }) {
  return (
    <li className="misenvios-tarjeta">
      <div className="misenvios-titulo">
        <div>
          <p className="misenvios-etiqueta">Código de envío</p>
          <h3>{envio.codigoEnvio}</h3>
        </div>
        {envio.estado !== "REGISTRADO" && (
          <span className="misenvios-estado" data-entregado={!actual || undefined}>
            {ETIQUETAS_ESTADO[envio.estado]}
          </span>
        )}
      </div>
      <p className="misenvios-ruta">
        <strong>{envio.origen}</strong>
        <span aria-hidden="true"> → </span>
        <strong>{envio.destino}</strong>
      </p>
      <dl className="misenvios-datos">
        <div><dt>Fecha de registro</dt><dd><time dateTime={envio.fechaRegistro}>{fecha(envio.fechaRegistro)}</time></dd></div>
        <div><dt>Modalidad</dt><dd>{envio.modalidad === "DOMICILIO" ? "Entrega a domicilio" : "Recojo en agencia"}</dd></div>
        {actual && (
          <>
            <div><dt>Paquete</dt><dd>{envio.descripcionPaquete || "Sin descripción"}</dd></div>
            <div><dt>Peso</dt><dd>{envio.pesoKg} kg</dd></div>
            <div><dt>Recibe</dt><dd>{envio.destinatarioNombre}</dd></div>
            {envio.direccionEntrega && <div><dt>Domicilio</dt><dd>{envio.direccionEntrega}</dd></div>}
          </>
        )}
        <div><dt>Importe</dt><dd>{moneda.format(envio.costoEnvio)}</dd></div>
        {envio.fechaEntrega && (
          <div><dt>Entregado</dt><dd><time dateTime={envio.fechaEntrega}>{fecha(envio.fechaEntrega)}</time></dd></div>
        )}
      </dl>
      <Link className="misenvios-seguimiento" to={`/seguimiento/${encodeURIComponent(envio.codigoEnvio)}`}>
        Ver recorrido
      </Link>
    </li>
  );
}

function Paginacion({
  datos,
  cambiarPagina,
}: {
  datos: PaginaEnviosCliente;
  cambiarPagina: (pagina: number) => void;
}) {
  if (datos.totalPaginas <= 1) return null;

  return (
    <nav className="misenvios-paginacion" aria-label="Páginas de envíos">
      <button type="button" disabled={datos.pagina <= 1} onClick={() => cambiarPagina(datos.pagina - 1)}>
        Anterior
      </button>
      <span aria-live="polite">Página {datos.pagina} de {datos.totalPaginas}</span>
      <button
        type="button"
        disabled={datos.pagina >= datos.totalPaginas}
        onClick={() => cambiarPagina(datos.pagina + 1)}
      >
        Siguiente
      </button>
    </nav>
  );
}

export default function MisEnvios() {
  const { usuario } = useAutenticacion();
  const [paginaActual, setPaginaActual] = useState(1);
  const [paginaAnterior, setPaginaAnterior] = useState(1);
  const [anterioresAbiertos, setAnterioresAbiertos] = useState(false);
  const actuales = useListaEnvios(usuario?.correo, false, paginaActual);
  const anteriores = useListaEnvios(
    usuario?.correo, true, paginaAnterior, anterioresAbiertos,
  );

  return (
    <main className="misenvios-pagina">
      <header className="misenvios-cabecera">
        <div>
          <h1>Mis envíos</h1>
          <p>{usuario?.nombre}, consulta tus envíos actuales y anteriores.</p>
        </div>
      </header>

      <section className="misenvios-seccion" aria-labelledby="misenvios-actuales">
        <div className="misenvios-seccion-cabecera">
          <h2 id="misenvios-actuales">Envíos actuales</h2>
          {actuales.datos && <span>{actuales.datos.totalElementos}</span>}
        </div>
        {actuales.cargando ? <p role="status">Cargando tus envíos actuales...</p>
          : actuales.error ? <p role="alert">{actuales.error}</p>
            : actuales.datos?.contenido.length ? (
              <>
                <ul className="misenvios-lista">
                  {actuales.datos.contenido.map((envio) => (
                    <TarjetaEnvio key={envio.codigoEnvio} envio={envio} actual />
                  ))}
                </ul>
                <Paginacion datos={actuales.datos} cambiarPagina={setPaginaActual} />
              </>
            ) : <p>No tienes envíos en curso. Consulta tus envíos anteriores más abajo.</p>}
      </section>

      <section className="misenvios-seccion misenvios-anteriores" aria-labelledby="misenvios-anteriores">
        <button
          id="misenvios-anteriores"
          className="misenvios-desplegar"
          type="button"
          aria-expanded={anterioresAbiertos}
          aria-controls="misenvios-historial"
          onClick={() => setAnterioresAbiertos((abiertos) => !abiertos)}
        >
          <span>Envíos anteriores</span>
          <span aria-hidden="true">{anterioresAbiertos ? "−" : "+"}</span>
        </button>
        {anterioresAbiertos && (
          <div id="misenvios-historial" className="misenvios-historial">
            {anteriores.cargando ? <p role="status">Cargando envíos anteriores...</p>
              : anteriores.error ? <p role="alert">{anteriores.error}</p>
                : anteriores.datos?.contenido.length ? (
                  <>
                    <p>{anteriores.datos.totalElementos} envíos entregados</p>
                    <ul className="misenvios-lista">
                      {anteriores.datos.contenido.map((envio) => (
                        <TarjetaEnvio key={envio.codigoEnvio} envio={envio} actual={false} />
                      ))}
                    </ul>
                    <Paginacion datos={anteriores.datos} cambiarPagina={setPaginaAnterior} />
                  </>
                ) : <p>Todavía no tienes envíos anteriores.</p>}
          </div>
        )}
      </section>
    </main>
  );
}
