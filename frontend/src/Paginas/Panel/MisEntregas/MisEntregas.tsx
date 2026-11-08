import { useState } from "react";
import ConfirmarEntrega from "../../../componentes/Entregas/ConfirmarEntrega";
import MarcoEntregas from "../../../componentes/Entregas/MarcoEntregas";
import TarjetaEntrega from "../../../componentes/Entregas/TarjetaEntrega";
import { useGestionEntregas } from "../../../componentes/Entregas/useGestionEntregas";
import { salirReparto } from "../../../Servicios/Entregas";
import type { Entrega } from "../../../Tipos/Entrega";
import "./MisEntregas.css";

export default function MisEntregas() {
  const gestion = useGestionEntregas("MOTORIZADO");
  const [entrega, setEntrega] = useState<Entrega | null>(null);
  const porSalir = gestion.envios.filter((envio) => envio.estado === "LLEGO_A_AGENCIA_DESTINO");
  const enReparto = gestion.envios.filter((envio) => envio.estado === "EN_REPARTO");
  const seleccionados = porSalir
    .filter((envio) => gestion.seleccion.includes(envio.id))
    .map((envio) => envio.id);

  return (
    <MarcoEntregas
      {...gestion}
      titulo="Mis entregas"
      descripcion="Tus envíos asignados, del más antiguo al más reciente"
    >
      <section className="misentregas-seccion" aria-labelledby="por-salir-titulo">
        <div className="entregas-herramientas">
          <h2 id="por-salir-titulo">Por salir ({porSalir.length})</h2>
          <button
            type="button"
            className="entregas-primario"
            disabled={gestion.ocupado || !seleccionados.length}
            onClick={() =>
              void gestion.ejecutar(
                () => salirReparto(seleccionados),
                `Saliste a reparto con ${seleccionados.length} envíos.`,
              )
            }
          >
            {gestion.ocupado ? "Guardando..." : `Salir a reparto (${seleccionados.length})`}
          </button>
        </div>

        {porSalir.length > 0 ? (
          <>
            <label className="misentregas-seleccion">
              <input
                type="checkbox"
                disabled={gestion.ocupado}
                checked={seleccionados.length === porSalir.length}
                onChange={(evento) =>
                  gestion.setSeleccion(evento.target.checked ? porSalir.map((envio) => envio.id) : [])
                }
              />
              Seleccionar todos
            </label>

            <ul className="entregas-lista">
              {porSalir.map((envio) => (
                <TarjetaEntrega
                  key={envio.id}
                  envio={envio}
                  bloqueada={gestion.ocupado}
                  seleccionada={gestion.seleccion.includes(envio.id)}
                  seleccionar={() => gestion.seleccionar(envio.id)}
                />
              ))}
            </ul>
          </>
        ) : (
          <p className="entregas-vacio">No hay envíos por salir.</p>
        )}
      </section>

      <section className="misentregas-seccion" aria-labelledby="en-reparto-titulo">
        <h2 id="en-reparto-titulo">En reparto ({enReparto.length})</h2>

        {enReparto.length > 0 ? (
          <ul className="entregas-lista">
            {enReparto.map((envio) => (
              <TarjetaEntrega key={envio.id} envio={envio}>
                <button
                  type="button"
                  className="entregas-primario"
                  disabled={gestion.ocupado}
                  onClick={() => setEntrega(envio)}
                >
                  Entregar
                </button>
              </TarjetaEntrega>
            ))}
          </ul>
        ) : (
          <p className="entregas-vacio">No hay envíos por entregar.</p>
        )}
      </section>

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
