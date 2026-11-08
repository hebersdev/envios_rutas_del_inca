import { useEffect, useState } from "react";
import { useAutenticacion } from "../../../Context/UseAutenticacion";
import { actualizarViaje, asignarPendientes, listarPendientes, listarViajes, type AccionViaje } from "../../../Servicios/Viajes";
import type { EnvioPendiente, EstadoViaje, Viaje } from "../../../Tipos/Viaje";
import TarjetaViaje from "../../../componentes/TarjetaViaje/TarjetaViaje";
import "./Viajes.css";

type Vista = "VIAJES" | "PENDIENTES" | "LLEGADAS";
interface Resultado {
  clave: string;
  viajes: Viaje[];
  pendientes: EnvioPendiente[];
  error: string;
}

export default function Viajes({ vista = "VIAJES" }: { vista?: Vista }) {
  const { usuario } = useAutenticacion();
  const [estado, setEstado] = useState<EstadoViaje | "">("");
  const [revision, setRevision] = useState(0);
  const [resultado, setResultado] = useState<Resultado | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [errorAccion, setErrorAccion] = useState("");
  const clave = `${vista}-${estado}-${revision}`;
  const cargando = resultado?.clave !== clave;

  useEffect(() => {
    let vigente = true;
    const consulta = vista === "PENDIENTES"
      ? listarPendientes().then((pendientes) => ({ pendientes, viajes: [] as Viaje[] }))
      : listarViajes(vista === "LLEGADAS" ? "EN_RUTA" : estado || undefined)
        .then((viajes) => ({ viajes, pendientes: [] as EnvioPendiente[] }));
    consulta.then((datos) => {
      if (vigente) setResultado({ clave, ...datos, error: "" });
    }).catch((error: unknown) => {
      if (vigente) setResultado({ clave, viajes: [], pendientes: [],
        error: error instanceof Error ? error.message : "No se pudo cargar la información." });
    });
    return () => { vigente = false; };
  }, [clave, vista, estado]);

  async function ejecutar(operacion: () => Promise<string>) {
    setOcupado(true);
    setErrorAccion("");
    setMensaje("");
    try {
      setMensaje(await operacion());
      setRevision((valor) => valor + 1);
    } catch (error: unknown) {
      setErrorAccion(error instanceof Error ? error.message : "No se pudo realizar la operación.");
    } finally {
      setOcupado(false);
    }
  }

  function cambiar(id: number, accion: AccionViaje) {
    void ejecutar(async () => {
      await actualizarViaje(id, accion);
      return "El viaje se actualizó correctamente.";
    });
  }

  const viajesVisibles = resultado?.viajes.filter((viaje) =>
    vista !== "LLEGADAS" || viaje.agenciaDestinoId === usuario?.agenciaId) ?? [];
  const titulo = vista === "PENDIENTES" ? "Envíos pendientes sin camión"
    : vista === "LLEGADAS" ? "Llegadas a mi sede" : "Viajes de mi sede";

  return (
    <section className="viajes-pagina">
      <header className="viajes-encabezado">
        <div><h1>{titulo}</h1><p>Coordinación de logística</p></div>
        {vista === "PENDIENTES" && (
          <button type="button" disabled={ocupado || cargando} onClick={() => {
            void ejecutar(async () => {
              const datos = await asignarPendientes();
              return `Se asignaron ${datos.asignados} envíos.`;
            });
          }}>Asignar pendientes ahora</button>
        )}
      </header>
      {vista === "VIAJES" && <label className="viajes-filtro">Estado
        <select value={estado} onChange={(evento) => setEstado(evento.target.value as EstadoViaje | "")}>
          <option value="">TODOS</option>
          {(["PROGRAMADO", "EN_RUTA", "LLEGADO", "FINALIZADO"] as const).map((opcion) =>
            <option key={opcion} value={opcion}>{opcion}</option>)}
        </select>
      </label>}
      {mensaje && <p role="status">{mensaje}</p>}
      {errorAccion && <p role="alert">{errorAccion}</p>}
      {cargando ? <p role="status">Cargando...</p> : resultado?.error ? (
        <div role="alert"><p>{resultado.error}</p>
          <button type="button" onClick={() => setRevision((valor) => valor + 1)}>Reintentar</button></div>
      ) : vista === "PENDIENTES" ? (
        resultado?.pendientes.length ? <ul className="viajes-pendientes">
          {resultado.pendientes.map(({ envio, motivo }) => (
            <li key={envio.id}>
              <strong>{envio.codigoEnvio}</strong>
              <p>Destino: {envio.destino} · {envio.pesoKg} kg</p>
              <p>{motivo}</p>
            </li>
          ))}
        </ul> : <p>No hay envíos pendientes sin camión.</p>
      ) : viajesVisibles.length ? (
        <div className="viajes-lista">{viajesVisibles.map((viaje) => (
          <TarjetaViaje key={`${viaje.id}-${revision}`} viaje={viaje} ocupado={ocupado || cargando}
            sedeId={usuario?.agenciaId ?? null} alActualizar={cambiar} />
        ))}</div>
      ) : <p>No hay viajes para mostrar.</p>}
    </section>
  );
}
