import { useEffect, useState, type FormEvent } from "react";
import { listarAgenciasActivas } from "../../Servicios/Agencias";
import { cotizarEnvio } from "../../Servicios/Tarifas";
import type { AgenciaResumen } from "../../Tipos/Agencia";
import "./Cotizar.css";
import type { Cotizacion, ModalidadEnvio } from "../../Tipos/Tarifa";
import ImagenFondoCotizacion from "../../imagenes/ImagenFondoCotizacion.png";
const monedaPen = new Intl.NumberFormat("es-PE", {
  style: "currency",
  currency: "PEN",
});

export default function Cotizar() {
  const [agencias, setAgencias] = useState<AgenciaResumen[]>([]);
  const [cargandoAgencias, setCargandoAgencias] = useState(true);
  const [errorAgencias, setErrorAgencias] = useState("");
  const [origenId, setOrigenId] = useState("");
  const [destinoId, setDestinoId] = useState("");
  const [pesoKg, setPesoKg] = useState("");
  const [modalidad, setModalidad] = useState<ModalidadEnvio>("AGENCIA");
  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [cargandoCotizacion, setCargandoCotizacion] = useState(false);
  const [errorCotizacion, setErrorCotizacion] = useState("");

  useEffect(() => {
    let vigente = true;
    listarAgenciasActivas()
      .then((resultado) => {
        if (vigente) {
          setAgencias(resultado);
          setErrorAgencias("");
        }
      })
      .catch((error: unknown) => {
        if (vigente) {
          setErrorAgencias(error instanceof Error ? error.message : "No se pudieron cargar las sedes.");
        }
      })
      .finally(() => {
        if (vigente) setCargandoAgencias(false);
      });
    return () => {
      vigente = false;
    };
  }, []);

  async function enviarCotizacion(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErrorCotizacion("");
    setCotizacion(null);
    setCargandoCotizacion(true);

    try {
      setCotizacion(await cotizarEnvio(
        Number(origenId),
        Number(destinoId),
        Number(pesoKg),
        modalidad,
      ));
    } catch (error: unknown) {
      setErrorCotizacion(error instanceof Error ? error.message : "No se pudo calcular la cotización.");
    } finally {
      setCargandoCotizacion(false);
    }
  }

  return (
    <main className="cotizar-pagina">
      <img src={ImagenFondoCotizacion} alt="Fondo de imagen de cotización" className="cotizar-fondo"/>
      <section className="cotizar-contenedor">
        <h1>Cotiza tu envío</h1>

        {errorAgencias ? (
          <div className="cotizar-error" role="alert">
            <p>{errorAgencias}</p>
            <button
              type="button"
              onClick={() => {
                setCargandoAgencias(true);
                setErrorAgencias("");
                listarAgenciasActivas()
                  .then(setAgencias)
                  .catch((error: unknown) => setErrorAgencias(
                    error instanceof Error ? error.message : "No se pudieron cargar las sedes.",
                  ))
                  .finally(() => setCargandoAgencias(false));
              }}
            >
              Reintentar
            </button>
          </div>
        ) : (
          <form className="cotizar-formulario" onSubmit={enviarCotizacion}>
            <label>
              ¿Dónde te encuentras?
              <select
                value={origenId}
                onChange={(evento) => {
                  setOrigenId(evento.target.value);
                  if (evento.target.value === destinoId) setDestinoId("");
                  setCotizacion(null);
                }}
                required
                disabled={cargandoAgencias}
              >
                <option value="">{cargandoAgencias ? "Cargando sedes..." : "Selecciona el origen"}</option>
                {agencias.map((agencia) => (
                  <option key={agencia.id} value={agencia.id}>
                    {agencia.nombre} — {agencia.ciudad}
                  </option>
                ))}
              </select>
            </label>
            <label>
              ¿A dónde lo llevamos?

              <select
                value={destinoId}
                onChange={(evento) => {
                  setDestinoId(evento.target.value);
                  setCotizacion(null);
                }}
                required
                disabled={cargandoAgencias}
              >
                <option value="">Selecciona el destino</option>
                {agencias.map((agencia) => (
                  <option key={agencia.id} value={agencia.id} disabled={String(agencia.id) === origenId}>
                    {agencia.nombre} — {agencia.ciudad}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Peso del paquete (kg)
              <input
                type="number"
                min="0.01"
                step="0.01"
                max="99999999"
                value={pesoKg}
                onChange={(evento) => {
                  setPesoKg(evento.target.value);
                  setCotizacion(null);
                }}
                required
              />
            </label>
            <label>
              ¿Dónde lo recibes?
              <select
                value={modalidad}
                onChange={(evento) => {
                  setModalidad(evento.target.value as ModalidadEnvio);
                  setCotizacion(null);
                }}
              >
                <option value="AGENCIA">Recojo en agencia</option>
                <option value="DOMICILIO">Entrega a domicilio</option>
              </select>
            </label>
            <button className="cotizar-boton" type="submit" disabled={cargandoAgencias || cargandoCotizacion}>
              {cargandoCotizacion ? "Cotizando..." : "Cotizar"}
            </button>
          </form>
        )}

        {errorCotizacion && <p className="cotizar-error" role="alert">{errorCotizacion}</p>}
        {cotizacion && (
          <div className="cotizar-resultado" aria-live="polite" aria-label="Resultado de cotización">
            <div className="cotizar-total">
              <span>Precio estimado:</span>
              <strong>{monedaPen.format(cotizacion.total)}</strong>
            </div>
          </div>
        )}

        <p className="cotizar-nota">
          Las tarifas son referenciales. Las tarifas incluyen IGV. 
          No incluye cargo adicional si desea asegurar su envío. 
        </p>
      </section>
    </main>
  );
}
