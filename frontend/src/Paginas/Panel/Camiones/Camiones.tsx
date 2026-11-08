import { useEffect, useState } from "react";
import ModalCamion from "../../../componentes/Panel/ModalCamion";
import { listarAgenciasActivas } from "../../../Servicios/Agencias";
import {
  cambiarActivoCamion,
  cambiarEstadoCamion,
  crearCamion,
  editarCamion,
  listarCamiones,
} from "../../../Servicios/Camiones";
import { useAutenticacion } from "../../../Context/UseAutenticacion";
import type { AgenciaResumen } from "../../../Tipos/Agencia";
import type { Camion, CamionRequest, EstadoCamion } from "../../../Tipos/Camion";
import "./Camiones.css";

type FiltroActivo = "todos" | "activos" | "inactivos";
type FiltroEstado = "todos" | EstadoCamion;

const nombresEstado: Record<EstadoCamion, string> = {
  DISPONIBLE: "Disponible",
  EN_VIAJE: "En viaje",
  EN_MANTENIMIENTO: "En mantenimiento",
};

export default function Camiones() {
  const { usuario } = useAutenticacion();
  const esAdministrador = usuario?.rol === "ADMINISTRADOR";
  const [camiones, setCamiones] = useState<Camion[]>([]);
  const [agencias, setAgencias] = useState<AgenciaResumen[]>([]);
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>("todos");
  const [filtroActivo, setFiltroActivo] = useState<FiltroActivo>("activos");
  const [agenciaActualId, setAgenciaActualId] = useState("");
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [modalAbierto, setModalAbierto] = useState(false);
  const [camionEditando, setCamionEditando] = useState<Camion | null>(null);
  const [camionProcesando, setCamionProcesando] = useState<number | null>(null);

  useEffect(() => {
    let vigente = true;
    listarAgenciasActivas()
      .then((resultado) => {
        if (vigente) setAgencias(resultado);
      })
      .catch((errorSolicitud: unknown) => {
        if (vigente) setError(errorSolicitud instanceof Error
          ? errorSolicitud.message
          : "No se pudieron cargar las sedes.");
      });
    return () => {
      vigente = false;
    };
  }, [intentoCarga]);

  useEffect(() => {
    let vigente = true;
    listarCamiones({
      estado: filtroEstado === "todos" ? undefined : filtroEstado,
      agenciaActualId: esAdministrador && agenciaActualId
        ? Number(agenciaActualId)
        : undefined,
      activo: filtroActivo === "todos" ? undefined : filtroActivo === "activos",
    })
      .then((resultado) => {
        if (vigente) setCamiones(resultado);
      })
      .catch((errorSolicitud: unknown) => {
        if (vigente) setError(errorSolicitud instanceof Error
          ? errorSolicitud.message
          : "No se pudieron cargar los camiones.");
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });
    return () => {
      vigente = false;
    };
  }, [filtroEstado, filtroActivo, agenciaActualId, esAdministrador, intentoCarga]);

  useEffect(() => {
    if (!aviso) return;
    const temporizador = window.setTimeout(() => setAviso(""), 3500);
    return () => window.clearTimeout(temporizador);
  }, [aviso]);

  function recargar() {
    setCargando(true);
    setError("");
    setIntentoCarga((actual) => actual + 1);
  }

  async function guardar(datos: CamionRequest) {
    if (camionEditando) {
      await editarCamion(camionEditando.id, datos);
    } else {
      await crearCamion(datos);
    }
    setModalAbierto(false);
    setAviso(camionEditando ? "El camión se actualizó correctamente." : "El camión se registró correctamente.");
    recargar();
  }

  async function cambiarEstado(camion: Camion) {
    if (camion.estado === "EN_VIAJE") return;
    const nuevoEstado = camion.estado === "DISPONIBLE" ? "EN_MANTENIMIENTO" : "DISPONIBLE";
    setCamionProcesando(camion.id);
    setError("");
    try {
      await cambiarEstadoCamion(camion.id, nuevoEstado);
      setAviso(`El camión ahora está ${nombresEstado[nuevoEstado].toLocaleLowerCase()}.`);
      recargar();
    } catch (errorSolicitud: unknown) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo cambiar el estado del camión.");
    } finally {
      setCamionProcesando(null);
    }
  }

  async function cambiarActivo(camion: Camion) {
    const nuevoActivo = !camion.activo;
    if (!nuevoActivo && !window.confirm(`¿Dar de baja el camión ${camion.placa}?`)) return;
    setCamionProcesando(camion.id);
    setError("");
    try {
      await cambiarActivoCamion(camion.id, nuevoActivo);
      setAviso(nuevoActivo ? "El camión se dio de alta." : "El camión se dio de baja.");
      recargar();
    } catch (errorSolicitud: unknown) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo cambiar la disponibilidad del camión.");
    } finally {
      setCamionProcesando(null);
    }
  }

  return (
    <section className="camiones-pagina">
      <header className="camiones-encabezado">
        <div>
          <p className="camiones-eyebrow">Logística</p>
          <h1>Camiones</h1>
          <p>Consulta y administra la flota y sus capacidades.</p>
        </div>
        {esAdministrador && (
          <button
            className="camiones-boton-principal"
            type="button"
            onClick={() => {
              setCamionEditando(null);
              setModalAbierto(true);
            }}
          >
            Nuevo camión
          </button>
        )}
      </header>

      <div className="camiones-filtros">
        <label>
          Estado
          <select
            value={filtroEstado}
            onChange={(evento) => {
              setCargando(true);
              setError("");
              setFiltroEstado(evento.target.value as FiltroEstado);
            }}
          >
            <option value="todos">Todos</option>
            <option value="DISPONIBLE">Disponible</option>
            <option value="EN_VIAJE">En viaje</option>
            <option value="EN_MANTENIMIENTO">En mantenimiento</option>
          </select>
        </label>
        {esAdministrador && (
          <>
            <label>
              Estado de registro
              <select
                value={filtroActivo}
                onChange={(evento) => {
                  setCargando(true);
                  setError("");
                  setFiltroActivo(evento.target.value as FiltroActivo);
                }}
              >
                <option value="todos">Todos</option>
                <option value="activos">De alta</option>
                <option value="inactivos">De baja</option>
              </select>
            </label>
            <label>
              Sede actual
              <select
                value={agenciaActualId}
                onChange={(evento) => {
                  setCargando(true);
                  setError("");
                  setAgenciaActualId(evento.target.value);
                }}
              >
                <option value="">Todas las sedes</option>
                {agencias.map((agencia) => (
                  <option key={agencia.id} value={agencia.id}>
                    {agencia.nombre} — {agencia.ciudad}
                  </option>
                ))}
              </select>
            </label>
          </>
        )}
      </div>

      {error && (
        <div className="camiones-error" role="alert">
          <p>{error}</p>
          <button type="button" onClick={recargar}>Reintentar</button>
        </div>
      )}

      <div className="camiones-tabla-contenedor">
        {cargando ? (
          <p className="camiones-estado">Cargando camiones...</p>
        ) : camiones.length === 0 ? (
          <p className="camiones-estado">No hay camiones que coincidan con los filtros.</p>
        ) : (
          <table className="camiones-tabla">
            <thead>
              <tr>
                <th scope="col">Placa</th>
                <th scope="col">Capacidad</th>
                <th scope="col">Sede base</th>
                <th scope="col">Sede actual</th>
                <th scope="col">Estado</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {camiones.map((camion) => (
                <tr key={camion.id}>
                  <td data-label="Placa">
                    <strong>{camion.placa}</strong>
                    {!camion.activo && <small className="camiones-baja">De baja</small>}
                  </td>
                  <td data-label="Capacidad">
                    {camion.pesoMaximoKg.toLocaleString("es-PE")} kg / {camion.volumenMaximoM.toLocaleString("es-PE")} m³
                  </td>
                  <td data-label="Sede base">{camion.agenciaBaseNombre}</td>
                  <td data-label="Sede actual">{camion.agenciaActualNombre}</td>
                  <td data-label="Estado">
                    <span className={`camiones-estado-etiqueta camiones-estado-etiqueta--${camion.estado.toLowerCase()}`}>
                      {nombresEstado[camion.estado]}
                    </span>
                  </td>
                  <td data-label="Acciones">
                    <div className="camiones-acciones">
                      {esAdministrador && (
                        <button
                          type="button"
                          title={camion.estado === "EN_VIAJE" ? "En viaje" : undefined}
                          disabled={camionProcesando === camion.id || camion.estado === "EN_VIAJE"}
                          onClick={() => {
                            setCamionEditando(camion);
                            setModalAbierto(true);
                          }}
                        >
                          Editar
                        </button>
                      )}
                      <button
                        type="button"
                        title={camion.estado === "EN_VIAJE" ? "En viaje" : undefined}
                        disabled={
                          camion.estado === "EN_VIAJE"
                          || !camion.activo
                          || camionProcesando === camion.id
                        }
                        onClick={() => void cambiarEstado(camion)}
                      >
                        {camion.estado === "DISPONIBLE"
                          ? "Cambiar a mantenimiento"
                          : camion.estado === "EN_MANTENIMIENTO" ? "Marcar disponible" : "En viaje"}
                      </button>
                      {esAdministrador && (
                        <button
                          type="button"
                          title={camion.estado === "EN_VIAJE" ? "En viaje" : undefined}
                          disabled={camionProcesando === camion.id || camion.estado === "EN_VIAJE"}
                          onClick={() => void cambiarActivo(camion)}
                        >
                          {camion.activo ? "Dar de baja" : "Dar de alta"}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalAbierto && (
        <ModalCamion
          key={camionEditando?.id ?? "nuevo"}
          agencias={agencias}
          camion={camionEditando}
          onCerrar={() => setModalAbierto(false)}
          onGuardar={guardar}
        />
      )}
    </section>
  );
}
