import { useState } from "react";
import ConfirmarEntrega from "../../../componentes/Entregas/ConfirmarEntrega";
import MarcoEntregas from "../../../componentes/Entregas/MarcoEntregas";
import TarjetaEntrega from "../../../componentes/Entregas/TarjetaEntrega";
import { useGestionEntregas } from "../../../componentes/Entregas/useGestionEntregas";
import { marcarListos } from "../../../Servicios/Entregas";
import type { Entrega } from "../../../Tipos/Entrega";
import "./PorEntregar.css";

type Pestana = "LLEGO_A_AGENCIA_DESTINO" | "LISTO_PARA_RECOJO";

export default function PorEntregar() {
  const gestion = useGestionEntregas("COUNTER");
  const [pestana, setPestana] = useState<Pestana>("LLEGO_A_AGENCIA_DESTINO");
  const [busqueda, setBusqueda] = useState("");
  const [entrega, setEntrega] = useState<Entrega | null>(null);
  const textoBusqueda = busqueda.toLocaleLowerCase("es-PE");
  const visibles = gestion.envios.filter((envio) => {
    const coincideBusqueda = `${envio.codigoEnvio} ${envio.destinatarioNombre}`
      .toLocaleLowerCase("es-PE")
      .includes(textoBusqueda);

    return envio.estado === pestana && coincideBusqueda;
  });
  const seleccionables = visibles.filter(
    (envio) => envio.modalidad === "AGENCIA" && envio.estado === "LLEGO_A_AGENCIA_DESTINO",
  );
  const seleccionados = seleccionables
    .filter((envio) => gestion.seleccion.includes(envio.id))
    .map((envio) => envio.id);

  return (
    <MarcoEntregas
      {...gestion}
      titulo="Por entregar"
      descripcion="Recepción y entrega en tu sede de destino"
    >
      <nav className="entregas-pestanas" aria-label="Estado de recepción">
        {(["LLEGO_A_AGENCIA_DESTINO", "LISTO_PARA_RECOJO"] as const).map((estado) => (
          <button
            key={estado}
            type="button"
            aria-pressed={pestana === estado}
            disabled={gestion.ocupado}
            onClick={() => {
              setPestana(estado);
              gestion.setSeleccion([]);
            }}
          >
            {estado === "LLEGO_A_AGENCIA_DESTINO" ? "Por recibir" : "Listos para recojo"}
          </button>
        ))}
      </nav>

      <div className="entregas-herramientas">
        <label>
          Buscar código o destinatario
          <input
            type="search"
            value={busqueda}
            onChange={(evento) => setBusqueda(evento.target.value)}
          />
        </label>

        {pestana === "LLEGO_A_AGENCIA_DESTINO" && (
          <>
            <label className="recepcion-seleccion">
              <input
                type="checkbox"
                disabled={!seleccionables.length || gestion.ocupado}
                checked={
                  seleccionables.length > 0 && seleccionados.length === seleccionables.length
                }
                onChange={(evento) =>
                  gestion.setSeleccion(
                    evento.target.checked ? seleccionables.map((envio) => envio.id) : [],
                  )
                }
              />
              Seleccionar visibles
            </label>

            <button
              type="button"
              className="entregas-primario"
              disabled={!seleccionados.length || gestion.ocupado}
              onClick={() =>
                void gestion.ejecutar(
                  () => marcarListos(seleccionados),
                  `Se marcaron ${seleccionados.length} envíos listos para recojo.`,
                )
              }
            >
              {gestion.ocupado
                ? "Guardando..."
                : `Marcar listos para recojo (${seleccionados.length})`}
            </button>
          </>
        )}
      </div>

      {visibles.length === 0 ? (
        <p className="entregas-vacio">No hay envíos por entregar.</p>
      ) : (
        <ul className="entregas-lista">
          {visibles.map((envio) => (
            <TarjetaEntrega
              key={envio.id}
              envio={envio}
              consulta={envio.modalidad === "DOMICILIO"}
              bloqueada={gestion.ocupado}
              seleccionada={gestion.seleccion.includes(envio.id)}
              seleccionar={
                envio.modalidad === "AGENCIA" &&
                pestana === "LLEGO_A_AGENCIA_DESTINO"
                  ? () => gestion.seleccionar(envio.id)
                  : undefined
              }
            >
              {envio.estado === "LISTO_PARA_RECOJO" && (
                <button
                  type="button"
                  className="entregas-primario"
                  onClick={() => setEntrega(envio)}
                >
                  Entregar
                </button>
              )}
            </TarjetaEntrega>
          ))}
        </ul>
      )}

      {entrega && (
        <ConfirmarEntrega
          key={entrega.id}
          envio={entrega}
          cerrar={() => setEntrega(null)}
          completada={() => {
            setEntrega(null);
            gestion.entregaCompletada();
          }}
        />
      )}
    </MarcoEntregas>
  );
}
