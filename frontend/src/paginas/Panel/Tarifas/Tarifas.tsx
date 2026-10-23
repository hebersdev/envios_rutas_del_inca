import { useEffect, useState } from "react";
import ModalTarifa from "../../../componentes/Panel/ModalTarifa";
import {
  cambiarEstadoTarifa,
  crearTarifa,
  editarTarifa,
  listarTarifas,
} from "../../../Servicios/Tarifas";
import type { Tarifa, TarifaRequest } from "../../../Tipos/Tarifa";
import "./Tarifas.css";

const monedaPen = new Intl.NumberFormat("es-PE", {
  style: "currency",
  currency: "PEN",
});

export default function Tarifas() {
  const [tarifas, setTarifas] = useState<Tarifa[]>([]);
  const [cargandoTarifas, setCargandoTarifas] = useState(true);
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [modalAbierto, setModalAbierto] = useState(false);
  const [tarifaEditando, setTarifaEditando] = useState<Tarifa | null>(null);
  const [tarifaProcesando, setTarifaProcesando] = useState<number | null>(null);

  useEffect(() => {
    let vigente = true;
    setCargandoTarifas(true);
    listarTarifas()
      .then((resultado) => {
        if (vigente) {
          setTarifas(resultado);
          setError("");
        }
      })
      .catch((errorSolicitud: unknown) => {
        if (vigente) {
          setError(errorSolicitud instanceof Error
            ? errorSolicitud.message
            : "No se pudieron cargar las tarifas.");
        }
      })
      .finally(() => {
        if (vigente) setCargandoTarifas(false);
      });
    return () => {
      vigente = false;
    };
  }, [intentoCarga]);

  useEffect(() => {
    if (!aviso) return;
    const temporizador = window.setTimeout(() => setAviso(""), 3500);
    return () => window.clearTimeout(temporizador);
  }, [aviso]);

  function recargar() {
    setCargandoTarifas(true);
    setIntentoCarga((actual) => actual + 1);
  }

  function abrirNueva() {
    setTarifaEditando(null);
    setModalAbierto(true);
  }

  async function guardar(datos: TarifaRequest) {
    if (tarifaEditando) {
      await editarTarifa(tarifaEditando.id, datos);
    } else {
      await crearTarifa(datos);
    }
    setModalAbierto(false);
    setAviso(tarifaEditando ? "La tarifa se actualizó correctamente." : "La tarifa se creó correctamente.");
    setCargandoTarifas(true);
    setIntentoCarga((actual) => actual + 1);
  }

  async function cambiarEstado(tarifa: Tarifa) {
    const nuevoEstado = !tarifa.activo;
    if (!nuevoEstado && !window.confirm("¿Desactivar esta tarifa?")) return;
    setTarifaProcesando(tarifa.id);
    setError("");
    try {
      await cambiarEstadoTarifa(tarifa.id, nuevoEstado);
      setAviso(nuevoEstado ? "La tarifa se activó correctamente." : "La tarifa se desactivó correctamente.");
      setCargandoTarifas(true);
      setIntentoCarga((actual) => actual + 1);
    } catch (errorSolicitud: unknown) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo cambiar el estado de la tarifa.");
    } finally {
      setTarifaProcesando(null);
    }
  }

  return (
    <section className="tarifas-pagina">
      <header className="tarifas-encabezado">
        <div>
          <p className="tarifas-eyebrow">Administración</p>
          <h1>Tarifas</h1>
          <p>Configura los mismos precios para todas las agencias según el peso y la modalidad de entrega.</p>
        </div>
        <button
          className="tarifas-boton-principal"
          type="button"
          onClick={abrirNueva}
          disabled={cargandoTarifas}
        >
          Nueva tarifa
        </button>
      </header>

      {error && (
        <div className="tarifas-error" role="alert">
          <p>{error}</p>
          <button type="button" onClick={recargar}>Reintentar</button>
        </div>
      )}

      <div className="tarifas-tabla-contenedor">
        {cargandoTarifas ? (
          <p className="tarifas-estado">Cargando tarifas...</p>
        ) : tarifas.length === 0 ? (
          <p className="tarifas-estado">No hay rangos de tarifas registrados.</p>
        ) : (
          <table className="tarifas-tabla">
            <thead>
              <tr>
                <th scope="col">Desde (kg)</th>
                <th scope="col">Hasta (kg)</th>
                <th scope="col">Precio base</th>
                <th scope="col">Recargo a domicilio</th>
                <th scope="col">Estado</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {tarifas.map((tarifa) => (
                <tr key={tarifa.id}>
                  <td data-label="Desde (kg)">{tarifa.rangoPesoMinKg.toFixed(2)}</td>
                  <td data-label="Hasta (kg)">{tarifa.rangoPesoMaxKg.toFixed(2)}</td>
                  <td data-label="Precio base">{monedaPen.format(tarifa.precioBase)}</td>
                  <td data-label="Recargo a domicilio">{monedaPen.format(tarifa.recargoDomicilio)}</td>
                  <td data-label="Estado">
                    <span className={`tarifas-estado-etiqueta${tarifa.activo ? " tarifas-estado-etiqueta--activa" : ""}`}>
                      {tarifa.activo ? "Activa" : "Inactiva"}
                    </span>
                  </td>
                  <td data-label="Acciones">
                    <div className="tarifas-acciones">
                      <button
                        type="button"
                        onClick={() => {
                          setTarifaEditando(tarifa);
                          setModalAbierto(true);
                        }}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        disabled={tarifaProcesando === tarifa.id}
                        onClick={() => void cambiarEstado(tarifa)}
                      >
                        {tarifaProcesando === tarifa.id
                          ? "Guardando..."
                          : tarifa.activo ? "Desactivar" : "Activar"}
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
        <ModalTarifa
          key={tarifaEditando?.id ?? "nueva"}
          tarifa={tarifaEditando}
          onCerrar={() => setModalAbierto(false)}
          onGuardar={guardar}
        />
      )}
    </section>
  );
}
