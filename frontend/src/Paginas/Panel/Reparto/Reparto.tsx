import { useState } from "react";
import { asignarMotorizado } from "../../../Servicios/Entregas";
import { useGestionEntregas } from "../../../componentes/Entregas/useGestionEntregas";
import MarcoEntregas from "../../../componentes/Entregas/MarcoEntregas";
import TarjetaEntrega from "../../../componentes/Entregas/TarjetaEntrega";
import "./Reparto.css";

export default function Reparto() {
  const gestion = useGestionEntregas("REPARTO");
  const [filtro, setFiltro] = useState("SIN_ASIGNAR");
  const [motorizado, setMotorizado] = useState("");
  const visibles = gestion.envios.filter((envio) => {
    if (filtro === "EN_REPARTO") return envio.estado === "EN_REPARTO";

    if (envio.estado !== "LLEGO_A_AGENCIA_DESTINO") return false;

    return filtro === "ASIGNADOS" ? envio.motorizadoId !== null : envio.motorizadoId === null;
  });
  const seleccionados = visibles
    .filter((envio) => gestion.seleccion.includes(envio.id))
    .map((envio) => envio.id);

  return (
    <MarcoEntregas
      {...gestion}
      titulo="Reparto"
      descripcion="Asigna los envíos a domicilio al personal de tu sede"
    >
      <div className="entregas-herramientas">
        <label>
          Mostrar
          <select
            value={filtro}
            disabled={gestion.ocupado}
            onChange={(evento) => {
              setFiltro(evento.target.value);
              gestion.setSeleccion([]);
            }}
          >
            <option value="SIN_ASIGNAR">Sin asignar</option>
            <option value="ASIGNADOS">Asignados</option>
            <option value="EN_REPARTO">En reparto</option>
          </select>
        </label>

        {filtro !== "EN_REPARTO" && (
          <>
            <label>
              Motorizado
              <select
                value={motorizado}
                disabled={gestion.ocupado}
                onChange={(evento) => setMotorizado(evento.target.value)}
              >
                <option value="">Selecciona un motorizado</option>
                {gestion.motorizados.map((persona) => (
                  <option key={persona.id} value={persona.id}>
                    {persona.nombre} · {persona.pendientes} pendientes
                  </option>
                ))}
              </select>
            </label>

            <button
              type="button"
              className="entregas-primario"
              disabled={gestion.ocupado || !motorizado || !seleccionados.length}
              onClick={() =>
                void gestion.ejecutar(
                  () => asignarMotorizado(seleccionados, Number(motorizado)),
                  `Se asignaron ${seleccionados.length} envíos al motorizado.`,
                )
              }
            >
              {gestion.ocupado ? "Asignando..." : `Asignar (${seleccionados.length})`}
            </button>
          </>
        )}
      </div>

      {gestion.motorizados.length === 0 && (
        <p className="reparto-aviso">No hay motorizados activos en tu sede.</p>
      )}

      {visibles.length > 0 && filtro !== "EN_REPARTO" && (
        <label className="reparto-seleccion">
          <input
            type="checkbox"
            disabled={gestion.ocupado}
            checked={seleccionados.length === visibles.length}
            onChange={(evento) =>
              gestion.setSeleccion(evento.target.checked ? visibles.map((envio) => envio.id) : [])
            }
          />
          Seleccionar visibles
        </label>
      )}

      {visibles.length === 0 ? (
        <p className="entregas-vacio">No hay envíos por entregar.</p>
      ) : (
        <ul className="entregas-lista">
          {visibles.map((envio) => (
            <TarjetaEntrega
              key={envio.id}
              envio={envio}
              seleccionada={gestion.seleccion.includes(envio.id)}
              bloqueada={gestion.ocupado}
              seleccionar={
                envio.estado === "LLEGO_A_AGENCIA_DESTINO"
                  ? () => gestion.seleccionar(envio.id)
                  : undefined
              }
            />
          ))}
        </ul>
      )}
    </MarcoEntregas>
  );
}
