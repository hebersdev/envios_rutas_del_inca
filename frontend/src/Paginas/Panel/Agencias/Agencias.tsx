import { useEffect, useMemo, useState } from "react";
import ModalAgencia from "../../../componentes/Panel/ModalAgencia";
import {
  cambiarEstadoAgencia,
  crearAgencia,
  editarAgencia,
  listarAgencias,
} from "../../../Servicios/Agencias";
import type { Agencia, AgenciaRequest } from "../../../Tipos/Agencia";
import "./Agencias.css";

type FiltroEstado = "todas" | "activas" | "inactivas";

export default function Agencias() {
  const [agencias, setAgencias] = useState<Agencia[]>([]);
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>("todas");
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [modalAbierto, setModalAbierto] = useState(false);
  const [agenciaEditando, setAgenciaEditando] = useState<Agencia | null>(null);
  const [agenciaProcesando, setAgenciaProcesando] = useState<number | null>(null);

  useEffect(() => {
    let vigente = true;
    const activo = filtroEstado === "todas" ? undefined : filtroEstado === "activas";
    listarAgencias(activo)
      .then((resultado) => {
        if (vigente) {
          setAgencias(resultado);
          setError("");
        }
      })
      .catch((errorSolicitud: unknown) => {
        if (vigente) {
          setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudieron cargar las sedes.");
        }
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
  }, [filtroEstado, intentoCarga]);

  useEffect(() => {
    if (!aviso) return;
    const temporizador = window.setTimeout(() => setAviso(""), 3500);
    return () => window.clearTimeout(temporizador);
  }, [aviso]);

  const agenciasFiltradas = useMemo(() => {
    const consulta = busqueda.trim().toLocaleLowerCase();
    if (!consulta) return agencias;
    return agencias.filter((agencia) => (
      `${agencia.nombre} ${agencia.ciudad}`.toLocaleLowerCase().includes(consulta)
    ));
  }, [agencias, busqueda]);

  function abrirNueva() {
    setAgenciaEditando(null);
    setModalAbierto(true);
  }

  function recargarAgencias() {
    setCargando(true);
    setIntentoCarga((actual) => actual + 1);
  }

  function abrirEdicion(agencia: Agencia) {
    setAgenciaEditando(agencia);
    setModalAbierto(true);
  }

  async function guardar(datos: AgenciaRequest) {
    if (agenciaEditando) {
      await editarAgencia(agenciaEditando.id, datos);
    } else {
      await crearAgencia(datos);
    }
    setModalAbierto(false);
    setAgenciaEditando(null);
    setAviso(agenciaEditando ? "La sede se actualizó correctamente." : "La sede se creó correctamente.");
    recargarAgencias();
  }

  async function cambiarEstado(agencia: Agencia) {
    const nuevoEstado = !agencia.activo;
    if (!nuevoEstado && !window.confirm(`¿Desactivar la sede ${agencia.nombre}?`)) return;

    setAgenciaProcesando(agencia.id);
    setError("");
    try {
      await cambiarEstadoAgencia(agencia.id, nuevoEstado);
      setAviso(nuevoEstado ? "La sede se activó correctamente." : "La sede se desactivó correctamente.");
      recargarAgencias();
    } catch (errorSolicitud) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo cambiar el estado de la sede.");
    } finally {
      setAgenciaProcesando(null);
    }
  }

  return (
    <section className="agencias-pagina">
      <header className="agencias-encabezado">
        <div>
          <p className="agencias-eyebrow">Administración</p>
          <h1>Sedes</h1>
          <p>Gestiona las sedes y sus ubicaciones.</p>
        </div>
        <button className="agencia-boton-principal" type="button" onClick={abrirNueva}>
          Nueva sede
        </button>
      </header>

      <div className="agencias-filtros">
        <label>
          Estado
          <select
            value={filtroEstado}
            onChange={(evento) => {
              setCargando(true);
              setFiltroEstado(evento.target.value as FiltroEstado);
            }}
          >
            <option value="todas">Todas</option>
            <option value="activas">Activas</option>
            <option value="inactivas">Inactivas</option>
          </select>
        </label>
        <label className="agencias-busqueda">
          Buscar
          <input
            type="search"
            value={busqueda}
            onChange={(evento) => setBusqueda(evento.target.value)}
            placeholder="Nombre o ciudad"
          />
        </label>
      </div>

      {error && (
        <div className="agencias-error" role="alert">
          <p>{error}</p>
          <button type="button" onClick={recargarAgencias}>Reintentar</button>
        </div>
      )}

      <div className="agencias-tabla-contenedor">
        {cargando ? (
          <p className="agencias-estado">Cargando sedes...</p>
        ) : agenciasFiltradas.length === 0 ? (
          <p className="agencias-estado">No hay sedes que coincidan.</p>
        ) : (
          <table className="agencias-tabla">
            <thead>
              <tr>
                <th scope="col">Nombre</th>
                <th scope="col">Dirección</th>
                <th scope="col">Ciudad</th>
                <th scope="col">Departamento</th>
                <th scope="col">Estado</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {agenciasFiltradas.map((agencia) => (
                <tr key={agencia.id}>
                  <td data-label="Nombre">{agencia.nombre}</td>
                  <td data-label="Dirección">{agencia.direccion}</td>
                  <td data-label="Ciudad">{agencia.ciudad}</td>
                  <td data-label="Departamento">{agencia.departamento}</td>
                  <td data-label="Estado">
                    <span className={`agencias-estado-etiqueta${agencia.activo ? " agencias-estado-etiqueta--activa" : ""}`}>
                      {agencia.activo ? "Activa" : "Inactiva"}
                    </span>
                  </td>
                  <td data-label="Acciones">
                    <div className="agencias-acciones">
                      <button type="button" onClick={() => abrirEdicion(agencia)}>
                        Editar
                      </button>
                      <button
                        type="button"
                        onClick={() => void cambiarEstado(agencia)}
                        disabled={agenciaProcesando === agencia.id}
                      >
                        {agenciaProcesando === agencia.id
                          ? "Guardando..."
                          : agencia.activo ? "Desactivar" : "Activar"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalAbierto && (
        <ModalAgencia
          key={agenciaEditando?.id ?? "nueva"}
          abierta={modalAbierto}
          agencia={agenciaEditando}
          onCerrar={() => setModalAbierto(false)}
          onGuardar={guardar}
        />
      )}
    </section>
  );
}
