import { useEffect, useMemo, useRef, useState } from "react";
import { listarEnvios } from "../../../Servicios/EnvioApi";
import type {
  EnvioResumen,
  EnvioDetalle,
  EstadoEnvio,
  PaginaEnvios,
} from "../../../Tipos/Envio";
import { ETIQUETAS_ESTADO } from "../../../Tipos/Envio";
import { useAutenticacion } from "../../../Context/UseAutenticacion";
import "./Envios.css";
import ModalRegistrarEnvio from "../ModalRegistrarEnvio/ModalRegistrarEnvio";
import DetalleEnvio from "../DetalleEnvio/DetalleEnvio";
import DescargasEnvio from "../../../componentes/DescargasEnvio/DescargasEnvio";

const ESTADOS_FILTRO: { valor: EstadoEnvio | ""; etiqueta: string }[] = [
  { valor: "", etiqueta: "Todos" },
  { valor: "REGISTRADO", etiqueta: "Registrado" },
  { valor: "ASIGNADO_A_VIAJE", etiqueta: "Listo para salir" },
  { valor: "EN_RUTA", etiqueta: "En ruta" },
  { valor: "LLEGO_A_AGENCIA_DESTINO", etiqueta: "Llegó a sede destino" },
  { valor: "LISTO_PARA_RECOJO", etiqueta: "Listo para recoger" },
  { valor: "EN_REPARTO", etiqueta: "En reparto" },
  { valor: "ENTREGADO", etiqueta: "Entregado" },
];

interface Consulta {
  estado: EstadoEnvio | "";
  codigo: string;
  pagina: number;
  version: number;
}

const CONSULTA_INICIAL: Consulta = {
  estado: "",
  codigo: "",
  pagina: 1,
  version: 0,
};

function colorEstado(estado: EstadoEnvio): string {
  switch (estado) {
    case "REGISTRADO":
      return "estado--registrado";
    case "ASIGNADO_A_VIAJE":
      return "estado--asignado";
    case "EN_RUTA":
      return "estado--en-ruta";
    case "LLEGO_A_AGENCIA_DESTINO":
      return "estado--llego";
    case "LISTO_PARA_RECOJO":
      return "estado--listo";
    case "EN_REPARTO":
      return "estado--reparto";
    case "ENTREGADO":
      return "estado--entregado";
  }
}

function formatearFecha(fecha: string): string {
  return new Date(fecha).toLocaleDateString("es-PE", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatearMoneda(valor: number): string {
  return new Intl.NumberFormat("es-PE", {
    style: "currency",
    currency: "PEN",
  }).format(valor);
}

export default function Envios() {
  const [pagina, setPagina] = useState<PaginaEnvios | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [consulta, setConsulta] = useState<Consulta>(CONSULTA_INICIAL);
  const [codigoBusqueda, setCodigoBusqueda] = useState("");
  const [modalRegistrar, setModalRegistrar] = useState(false);
  const [envioDetalle, setEnvioDetalle] = useState<EnvioResumen | null>(null);
  const [envioRegistrado, setEnvioRegistrado] = useState<EnvioDetalle | null>(null);
  const intentoCargaRef = useRef(0);

  const { usuario } = useAutenticacion();
  const agenciaOrigenId = usuario?.agenciaId ?? null;

  useEffect(() => {
    let vigente = true;
    intentoCargaRef.current += 1;
    const intento = intentoCargaRef.current;

    listarEnvios(
      consulta.estado || undefined,
      consulta.codigo || undefined,
      consulta.pagina,
    )
      .then((resultado) => {
        if (vigente && intento === intentoCargaRef.current) {
          setPagina(resultado);
        }
      })
      .catch((err: unknown) => {
        if (vigente && intento === intentoCargaRef.current) {
          setError(
            err instanceof Error
              ? err.message
              : "No se pudieron cargar los envíos.",
          );
        }
      })
      .finally(() => {
        if (vigente && intento === intentoCargaRef.current) {
          setCargando(false);
        }
      });

    return () => {
      vigente = false;
    };
  }, [consulta]);

  useEffect(() => {
    if (!aviso) return;
    const t = window.setTimeout(() => setAviso(""), 4000);
    return () => window.clearTimeout(t);
  }, [aviso]);

  function recargar() {
    setCargando(true);
    setError("");
    setConsulta((c) => ({ ...c, version: c.version + 1 }));
  }

  function buscar(e: React.FormEvent) {
    e.preventDefault();
    setCargando(true);
    setError("");
    setConsulta((c) => ({
      ...c,
      codigo: codigoBusqueda.trim(),
      pagina: 1,
      version: c.version + 1,
    }));
  }

  function cambiarEstadoFiltro(nuevo: EstadoEnvio | "") {
    setCargando(true);
    setError("");
    setConsulta((c) => ({
      ...c,
      estado: nuevo,
      pagina: 1,
      version: c.version + 1,
    }));
  }

  function cambiarPagina(delta: number) {
    setCargando(true);
    setError("");
    setConsulta((c) => ({
      ...c,
      pagina: c.pagina + delta,
      version: c.version + 1,
    }));
  }

  function alRegistrar(envio: EnvioDetalle) {
    setModalRegistrar(false);
    setEnvioRegistrado(envio);
    setAviso("");
    setCodigoBusqueda("");
    setCargando(true);
    setError("");
    setConsulta((c) => ({
      estado: "",
      codigo: "",
      pagina: 1,
      version: c.version + 1,
    }));
  }

  function alActualizarEstado() {
    setEnvioDetalle(null);
    setAviso("El estado del envío fue actualizado.");
    recargar();
  }

  const envios: EnvioResumen[] = useMemo(
    () => pagina?.contenido ?? [],
    [pagina],
  );
  const totalPaginas = pagina?.totalPaginas ?? 1;
  const paginaActual = consulta.pagina;
  const estadoFiltro = consulta.estado;
  const primerEnvio = pagina ? (paginaActual - 1) * pagina.tamanio + 1 : 0;
  const ultimoEnvio = pagina
    ? Math.min(paginaActual * pagina.tamanio, pagina.totalElementos)
    : 0;

  return (
    <section className="envios-pagina">
      <header className="envios-encabezado">
        <div>
          <p className="envios-eyebrow">Counter</p>
          <h1>Envíos</h1>
          <p>Registra y gestiona los envíos de tu sede.</p>
        </div>
        <button
          id="btn-nuevo-envio"
          className="envios-btn-principal"
          type="button"
          onClick={() => setModalRegistrar(true)}
          disabled={agenciaOrigenId === null}
        >
          + Nuevo envío
        </button>
      </header>

      {aviso && (
        <div className="envios-aviso" role="status">
          {aviso}
        </div>
      )}

      {envioRegistrado && (
        <section className="envios-comprobantes" aria-label="Envío registrado">
          <header>
            <div>
              <h2>Envío registrado correctamente</h2>
              <p>Código de seguimiento: <strong>{envioRegistrado.codigoEnvio}</strong></p>
            </div>
            <button type="button" onClick={() => setEnvioRegistrado(null)} aria-label="Cerrar documentos">×</button>
          </header>
          <DescargasEnvio envioId={envioRegistrado.id} codigo={envioRegistrado.codigoEnvio} />
        </section>
      )}

      <div className="envios-filtros">
        <label>
          Estado
          <select
            id="filtro-estado"
            value={estadoFiltro}
            onChange={(e) =>
              cambiarEstadoFiltro(e.target.value as EstadoEnvio | "")
            }
          >
            {ESTADOS_FILTRO.map((op) => (
              <option key={op.valor} value={op.valor}>
                {op.etiqueta}
              </option>
            ))}
          </select>
        </label>
        <form className="envios-busqueda-form" onSubmit={buscar}>
          <label>
            Código de envío
            <div className="envios-busqueda-group">
              <input
                id="busqueda-codigo"
                type="search"
                placeholder="Código de envío"
                value={codigoBusqueda}
                onChange={(e) => setCodigoBusqueda(e.target.value)}
              />
              <button type="submit" className="envios-btn-buscar">
                Buscar
              </button>
            </div>
          </label>
        </form>
      </div>

      {error && (
        <div className="envios-error" role="alert">
          <p>{error}</p>
          <button type="button" onClick={recargar}>
            Reintentar
          </button>
        </div>
      )}

      <div className="envios-tabla-contenedor">
        {cargando ? (
          <p className="envios-estado-vacio">Cargando envíos...</p>
        ) : envios.length === 0 ? (
          <p className="envios-estado-vacio">
            No se encontraron envíos con esos criterios.
          </p>
        ) : (
          <table className="envios-tabla">
            <thead>
              <tr>
                <th scope="col">Código</th>
                <th scope="col">Remitente</th>
                <th scope="col">Destino</th>
                <th scope="col">Estado</th>
                <th scope="col">Modalidad</th>
                <th scope="col">Peso</th>
                <th scope="col">Costo</th>
                <th scope="col">Fecha</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {envios.map((envio) => (
                <tr key={envio.id}>
                  <td data-label="Código">
                    <span className="envios-codigo">{envio.codigoEnvio}</span>
                  </td>
                  <td data-label="Remitente">{envio.remitenteNombre}</td>
                  <td data-label="Destino">{envio.destinoNombre}</td>
                  <td data-label="Estado">
                    <span
                      className={`envios-estado-badge ${colorEstado(envio.estado)}`}
                    >
                      {ETIQUETAS_ESTADO[envio.estado]}
                    </span>
                  </td>
                  <td data-label="Modalidad">
                    <span
                      className={`envios-modalidad-badge ${
                        envio.modalidad === "DOMICILIO"
                          ? "modalidad--domicilio"
                          : "modalidad--agencia"
                      }`}
                    >
                      {envio.modalidad === "DOMICILIO"
                        ? "🏠 Domicilio"
                        : "🏢 Agencia"}
                    </span>
                  </td>
                  <td data-label="Peso">{envio.pesoKg} kg</td>
                  <td data-label="Costo">
                    {formatearMoneda(envio.costoEnvio)}
                  </td>
                  <td data-label="Fecha">
                    {formatearFecha(envio.fechaRegistro)}
                  </td>
                  <td data-label="Acciones">
                    <button
                      type="button"
                      className="envios-btn-detalle"
                      onClick={() => setEnvioDetalle(envio)}
                    >
                      Ver detalle
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {!cargando && totalPaginas > 1 && (
        <div className="envios-paginacion">
          <button
            type="button"
            disabled={paginaActual <= 1}
            onClick={() => cambiarPagina(-1)}
          >
            ← Anterior
          </button>
          <span>
            Página {paginaActual} de {totalPaginas}
          </span>
          <button
            type="button"
            disabled={paginaActual >= totalPaginas}
            onClick={() => cambiarPagina(1)}
          >
            Siguiente →
          </button>
        </div>
      )}

      {!cargando && pagina && (
        <p className="envios-total">
          {pagina.totalElementos === 0
            ? "Sin envíos"
            : `Mostrando ${primerEnvio}-${ultimoEnvio} de ${pagina.totalElementos} envío${
                pagina.totalElementos !== 1 ? "s" : ""
              }`}
        </p>
      )}

      {modalRegistrar && agenciaOrigenId !== null && (
        <ModalRegistrarEnvio
          onCerrar={() => setModalRegistrar(false)}
          onRegistrado={alRegistrar}
          agenciaOrigenId={agenciaOrigenId}
        />
      )}

      {envioDetalle && (
        <DetalleEnvio
          envioId={envioDetalle.id}
          onCerrar={() => setEnvioDetalle(null)}
          onEstadoActualizado={alActualizarEstado}
        />
      )}
    </section>
  );
}
