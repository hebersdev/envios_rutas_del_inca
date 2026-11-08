import { useEffect, useState, type FormEvent } from "react";
import { listarAgenciasActivas } from "../../Servicios/Agencias";
import { cotizarEnvio } from "../../Servicios/Tarifas";
import type { AgenciaResumen } from "../../Tipos/Agencia";
import "./Cotizar.css";
import type { Cotizacion, ModalidadEnvio } from "../../Tipos/Tarifa";
import ImagenFondoCotizacion from "../../imagenes/ImagenFondoCotizacion.webp";

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
  const [altoCm, setAltoCm] = useState("");
  const [anchoCm, setAnchoCm] = useState("");
  const [largoCm, setLargoCm] = useState("");
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
          setErrorAgencias(
            error instanceof Error
              ? error.message
              : "No se pudieron cargar las sedes.",
          );
        }
      })
      .finally(() => {
        if (vigente) setCargandoAgencias(false);
      });
    return () => {
      vigente = false;
    };
  }, []);

  function limpiarCotizacion() {
    setCotizacion(null);
    setErrorCotizacion("");
  }

  async function enviarCotizacion(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErrorCotizacion("");

    const algunaDimension = altoCm || anchoCm || largoCm;
    const todasDimensiones = altoCm && anchoCm && largoCm;

    if (algunaDimension && !todasDimensiones) {
      setCotizacion(null);
      setErrorCotizacion(
        "Completa las tres medidas o déjalas todas vacías.",
      );
      return;
    }

    setCotizacion(null);
    setCargandoCotizacion(true);

    try {
      const resultado = await cotizarEnvio({
        origenId: Number(origenId),
        destinoId: Number(destinoId),
        pesoKg: Number(pesoKg),
        modalidad,
        altoCm: todasDimensiones ? Number(altoCm) : undefined,
        anchoCm: todasDimensiones ? Number(anchoCm) : undefined,
        largoCm: todasDimensiones ? Number(largoCm) : undefined,
      });
      setCotizacion(resultado);
    } catch (error: unknown) {
      setErrorCotizacion(
        error instanceof Error
          ? error.message
          : "No se pudo calcular la cotización.",
      );
    } finally {
      setCargandoCotizacion(false);
    }
  }

  function recargarAgencias() {
    setCargandoAgencias(true);
    setErrorAgencias("");
    listarAgenciasActivas()
      .then(setAgencias)
      .catch((error: unknown) =>
        setErrorAgencias(
          error instanceof Error
            ? error.message
            : "No se pudieron cargar las sedes.",
        ),
      )
      .finally(() => setCargandoAgencias(false));
  }

  const cotizaPorVolumen =
    cotizacion !== null && cotizacion.pesoFacturableKg > cotizacion.pesoKg;

  return (
    <main className="cotizar-pagina">
      <img
        src={ImagenFondoCotizacion}
        alt="Fondo de imagen de cotización"
        className="cotizar-fondo"
      />
      <section className="cotizar-contenedor">
        <h1>Cotiza tu envío</h1>

        {errorAgencias ? (
          <div className="cotizar-error" role="alert">
            <p>{errorAgencias}</p>
            <button type="button" onClick={recargarAgencias}>
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
                  limpiarCotizacion();
                }}
                required
                disabled={cargandoAgencias}
              >
                <option value="">
                  {cargandoAgencias
                    ? "Cargando sedes..."
                    : "Selecciona el origen"}
                </option>
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
                  limpiarCotizacion();
                }}
                required
                disabled={cargandoAgencias}
              >
                <option value="">Selecciona el destino</option>
                {agencias.map((agencia) => (
                  <option
                    key={agencia.id}
                    value={agencia.id}
                    disabled={String(agencia.id) === origenId}
                  >
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
                  limpiarCotizacion();
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
                  limpiarCotizacion();
                }}
              >
                <option value="AGENCIA">Recojo en agencia</option>
                <option value="DOMICILIO">Entrega a domicilio</option>
              </select>
            </label>

            <details className="cotizar-medidas">
              <summary>Agregar medidas del paquete (opcional)</summary>
              <p className="cotizar-medidas-ayuda">
                Si tu paquete es grande pero liviano, las medidas ayudan a
                calcular un precio más justo. Si no las conoces, puedes
                cotizar solo por peso.
              </p>
              <div className="cotizar-medidas-grid">
                <label>
                  Alto (cm)
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={altoCm}
                    onChange={(evento) => {
                      setAltoCm(evento.target.value);
                      limpiarCotizacion();
                    }}
                  />
                </label>
                <label>
                  Ancho (cm)
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={anchoCm}
                    onChange={(evento) => {
                      setAnchoCm(evento.target.value);
                      limpiarCotizacion();
                    }}
                  />
                </label>
                <label>
                  Largo (cm)
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={largoCm}
                    onChange={(evento) => {
                      setLargoCm(evento.target.value);
                      limpiarCotizacion();
                    }}
                  />
                </label>
              </div>
            </details>

            <button
              className="cotizar-boton"
              type="submit"
              disabled={cargandoAgencias || cargandoCotizacion}
            >
              {cargandoCotizacion ? "Cotizando..." : "Cotizar"}
            </button>
          </form>
        )}

        {errorCotizacion && (
          <p className="cotizar-error" role="alert">
            {errorCotizacion}
          </p>
        )}

        {cotizacion && (
          <div
            className="cotizar-resultado"
            aria-live="polite"
            aria-label="Resultado de cotización"
          >
            <div className="cotizar-total">
              <span>Precio estimado:</span>
              <strong>{monedaPen.format(cotizacion.total)}</strong>
            </div>
            {cotizaPorVolumen && (
              <p className="cotizar-detalle-volumen">
                Tarifa por volumen aplicada:{" "}
                <strong>{cotizacion.pesoFacturableKg} kg</strong>.
              </p>
            )}
          </div>
        )}

        <p className="cotizar-nota">
          Las tarifas son referenciales. Las tarifas incluyen IGV. No incluye
          cargo adicional si desea asegurar su envío.
        </p>
      </section>
    </main>
  );
}
