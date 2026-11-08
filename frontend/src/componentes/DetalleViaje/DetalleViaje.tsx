import { useEffect, useState } from "react";
import { obtenerViaje } from "../../Servicios/Viajes";
import type { ViajeDetalle } from "../../Tipos/Viaje";
import "./DetalleViaje.css";

function medidas(envio: ViajeDetalle["envios"][number]) {
  if (envio.altoCm == null || envio.anchoCm == null || envio.largoCm == null) {
    return "Sin medidas completas";
  }

  return `${envio.altoCm} × ${envio.anchoCm} × ${envio.largoCm} cm`;
}

export default function DetalleViaje({ id }: { id: number }) {
  const [detalle, setDetalle] = useState<ViajeDetalle | null>(null);
  const [error, setError] = useState("");
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    let vigente = true;

    obtenerViaje(id)
      .then((datos) => {
        if (vigente) setDetalle(datos);
      })
      .catch((fallo: unknown) => {
        if (!vigente) return;

        setError(fallo instanceof Error ? fallo.message : "No se pudo cargar el detalle.");
      });

    return () => {
      vigente = false;
    };
  }, [id, intento]);

  if (error) {
    return (
      <div className="viaje-detalle-error" role="alert">
        <p>{error}</p>
        <button
          type="button"
          onClick={() => {
            setError("");
            setIntento((valor) => valor + 1);
          }}
        >
          Reintentar
        </button>
      </div>
    );
  }

  if (!detalle) {
    return (
      <p className="viaje-detalle-cargando" role="status">
        Cargando envíos...
      </p>
    );
  }

  return (
    <section className="viaje-detalle" aria-label="Envíos del viaje">
      <header>
        <h3>Códigos de envío</h3>
        <span>{detalle.envios.length} en total</span>
      </header>

      <ul className="viaje-envios">
        {detalle.envios.map((envio) => (
          <li key={envio.id}>
            <div className="viaje-envio-identidad">
              <strong>Código: {envio.codigoEnvio}</strong>
              <span>Destino: {envio.destino}</span>
            </div>

            <dl>
              <div>
                <dt>Peso</dt>
                <dd>{envio.pesoKg} kg</dd>
              </div>
              <div>
                <dt>Medidas</dt>
                <dd>{medidas(envio)}</dd>
              </div>
            </dl>
          </li>
        ))}
      </ul>
    </section>
  );
}
