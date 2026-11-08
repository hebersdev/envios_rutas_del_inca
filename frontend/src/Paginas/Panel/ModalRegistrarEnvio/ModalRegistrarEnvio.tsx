import { useEffect, useState } from "react";

import "./ModalRegistrarEnvio.css";
import { registrarEnvio } from "../../../Servicios/EnvioApi";
import type {
  Cotizacion,
  EnvioRequest,
  EnvioDetalle,
  ModalidadEnvio,
} from "../../../Tipos/Envio";
import { clienteApi } from "../../../Servicios/ClienteApi";

interface Props {
  onCerrar: () => void;
  onRegistrado: (envio: EnvioDetalle) => void;
  agenciaOrigenId: number;
}

interface Agencia {
  id: number;
  nombre: string;
  ciudad: string;
}

interface ClienteEncontrado {
  id: number;
  nombre: string;
  documento: string;
  correo: string | null;
  telefono: string | null;
}

const MODALIDADES: { valor: ModalidadEnvio; etiqueta: string }[] = [
  { valor: "AGENCIA", etiqueta: "Recojo en agencia" },
  { valor: "DOMICILIO", etiqueta: "Entrega a domicilio" },
];

export default function ModalRegistrarEnvio({
  onCerrar,
  onRegistrado,
  agenciaOrigenId,
}: Props) {
  const [agencias, setAgencias] = useState<Agencia[]>([]);

  const [remitenteNombre, setRemitenteNombre] = useState("");
  const [remitenteDocumento, setRemitenteDocumento] = useState("");
  const [remitenteTelefono, setRemitenteTelefono] = useState("");
  const [clienteVinculadoId, setClienteVinculadoId] = useState<number | null>(
    null,
  );
  const [buscandoCliente, setBuscandoCliente] = useState(false);
  const [errorBusqueda, setErrorBusqueda] = useState("");

  const [agenciaDestinoId, setAgenciaDestinoId] = useState<number | "">("");
  const [modalidad, setModalidad] = useState<ModalidadEnvio>("AGENCIA");
  const [descripcionPaquete, setDescripcionPaquete] = useState("");
  const [pesoKg, setPesoKg] = useState("");
  const [altoCm, setAltoCm] = useState("");
  const [anchoCm, setAnchoCm] = useState("");
  const [largoCm, setLargoCm] = useState("");
  const [destinatarioNombre, setDestinatarioNombre] = useState("");
  const [destinatarioDocumento, setDestinatarioDocumento] = useState("");
  const [destinatarioTelefono, setDestinatarioTelefono] = useState("");
  const [direccionEntrega, setDireccionEntrega] = useState("");

  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [cotizacionClave, setCotizacionClave] = useState("");

  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState("");

  const pesoNumero = Number(pesoKg);
  const claveActual =
    agenciaDestinoId && pesoNumero > 0
      ? `${agenciaDestinoId}|${pesoKg}|${altoCm}|${anchoCm}|${largoCm}|${modalidad}`
      : "";
  const cotizando = claveActual !== "" && claveActual !== cotizacionClave;
  const cotizacionVigente =
    claveActual !== "" && claveActual === cotizacionClave ? cotizacion : null;

  useEffect(() => {
    let vigente = true;

    clienteApi<Agencia[]>("/api/agencias/activas")
      .then((data) => {
        if (vigente) setAgencias(data);
      })
      .catch(() => {
        if (vigente) setError("No se pudo cargar la lista de agencias.");
      });

    return () => {
      vigente = false;
    };
  }, []);

  useEffect(() => {
    if (!claveActual) return;

    const controller = new AbortController();
    const clave = claveActual;

    const params = new URLSearchParams({
      origenId: String(agenciaOrigenId),
      destinoId: String(agenciaDestinoId),
      pesoKg: String(pesoNumero),
      modalidad,
    });

    if (altoCm && anchoCm && largoCm) {
      params.set("altoCm", altoCm);
      params.set("anchoCm", anchoCm);
      params.set("largoCm", largoCm);
    }

    clienteApi<Cotizacion>(`/api/cotizaciones?${params.toString()}`)
      .then((data) => {
        if (!controller.signal.aborted) {
          setCotizacion(data);
          setCotizacionClave(clave);
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setCotizacion(null);
          setCotizacionClave(clave);
        }
      });

    return () => {
      controller.abort();
    };
  }, [
    claveActual,
    agenciaDestinoId,
    pesoNumero,
    modalidad,
    agenciaOrigenId,
    altoCm,
    anchoCm,
    largoCm,
  ]);

  function alEditarRemitenteDocumento(valor: string) {
    setRemitenteDocumento(valor);
    if (clienteVinculadoId !== null) {
      setClienteVinculadoId(null);
    }
    setErrorBusqueda("");
  }

  async function buscarCliente() {
    const documento = remitenteDocumento.trim();
    if (!documento) return;

    setBuscandoCliente(true);
    setErrorBusqueda("");

    try {
      const encontrado = await clienteApi<ClienteEncontrado>(
        `/api/clientes/buscar?documento=${encodeURIComponent(documento)}`,
      );
      setClienteVinculadoId(encontrado.id);
      setRemitenteNombre(encontrado.nombre);
      setRemitenteDocumento(encontrado.documento);
      setRemitenteTelefono(encontrado.telefono ?? "");
    } catch {
      setClienteVinculadoId(null);
      setErrorBusqueda(
        "No encontramos una cuenta con ese documento. Puedes completar los datos manualmente.",
      );
    } finally {
      setBuscandoCliente(false);
    }
  }

  async function registrar(e: React.FormEvent) {
    e.preventDefault();

    if (!remitenteNombre.trim()) {
      setError("Indica el nombre del remitente.");
      return;
    }
    if (!remitenteDocumento.trim()) {
      setError("Indica el documento del remitente.");
      return;
    }
    if (!agenciaDestinoId) {
      setError("Selecciona la sede de destino.");
      return;
    }
    if (modalidad === "DOMICILIO" && !direccionEntrega.trim()) {
      setError("Indica la dirección de entrega.");
      return;
    }

    setEnviando(true);
    setError("");

    const solicitud: EnvioRequest = {
      clienteId: clienteVinculadoId,
      agenciaDestinoId: Number(agenciaDestinoId),
      modalidad,
      descripcionPaquete: descripcionPaquete.trim(),
      pesoKg: Number(pesoKg),
      altoCm: altoCm ? Number(altoCm) : null,
      anchoCm: anchoCm ? Number(anchoCm) : null,
      largoCm: largoCm ? Number(largoCm) : null,
      remitenteNombre: remitenteNombre.trim(),
      remitenteDocumento: remitenteDocumento.trim(),
      remitenteTelefono: remitenteTelefono.trim() || null,
      destinatarioNombre: destinatarioNombre.trim(),
      destinatarioDocumento: destinatarioDocumento.trim(),
      destinatarioTelefono: destinatarioTelefono.trim(),
      direccionEntrega:
        modalidad === "DOMICILIO" ? direccionEntrega.trim() : null,
    };

    try {
      const envio = await registrarEnvio(solicitud);
      onRegistrado(envio);
    } catch (err: unknown) {
      setError(
        err instanceof Error ? err.message : "No se pudo registrar el envío.",
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div
        className="modal-panel"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <header className="modal-cabecera">
          <h2>Nuevo envío</h2>
          <button
            type="button"
            className="modal-cerrar"
            onClick={onCerrar}
            aria-label="Cerrar"
          >
            ×
          </button>
        </header>

        <form className="modal-formulario" onSubmit={registrar}>
          <fieldset className="modal-seccion">
            <legend>Remitente</legend>
            <div className="modal-campo">
              <label htmlFor="remitenteDocumento">Documento (DNI o RUC)</label>
              <div className="modal-busqueda">
                <input
                  id="remitenteDocumento"
                  type="text"
                  value={remitenteDocumento}
                  onChange={(e) =>
                    alEditarRemitenteDocumento(
                      e.target.value.replace(/\D/g, ""),
                    )
                  }
                  placeholder="Documento del remitente"
                  maxLength={11}
                  required
                />
                <button
                  type="button"
                  onClick={buscarCliente}
                  disabled={buscandoCliente || !remitenteDocumento.trim()}
                >
                  {buscandoCliente ? "Buscando..." : "Buscar"}
                </button>
              </div>
            </div>
            <div className="modal-campo">
              <label htmlFor="remitenteNombre">Nombre completo</label>
              <input
                id="remitenteNombre"
                type="text"
                value={remitenteNombre}
                onChange={(e) => setRemitenteNombre(e.target.value)}
                maxLength={150}
                required
              />
            </div>
            <div className="modal-campo">
              <label htmlFor="remitenteTelefono">Teléfono</label>
              <input
                id="remitenteTelefono"
                type="tel"
                value={remitenteTelefono}
                onChange={(e) => setRemitenteTelefono(e.target.value)}
                maxLength={20}
              />
            </div>
            {clienteVinculadoId !== null && (
              <p className="modal-cliente-encontrado">
                Cliente registrado, los envíos se vincularán a su cuenta.
              </p>
            )}
            {errorBusqueda && (
              <p className="modal-cliente-advertencia" role="status">
                {errorBusqueda}
              </p>
            )}
          </fieldset>

          <fieldset className="modal-seccion">
            <legend>Ruta y modalidad</legend>
            <div className="modal-campo">
              <label htmlFor="agenciaDestino">Sede destino</label>
              <select
                id="agenciaDestino"
                value={agenciaDestinoId}
                onChange={(e) =>
                  setAgenciaDestinoId(
                    e.target.value ? Number(e.target.value) : "",
                  )
                }
                required
              >
                <option value="">Selecciona una sede</option>
                {agencias
                  .filter((agencia) => agencia.id !== agenciaOrigenId)
                  .map((agencia) => (
                    <option key={agencia.id} value={agencia.id}>
                      {agencia.nombre} — {agencia.ciudad}
                    </option>
                  ))}
              </select>
            </div>
            <div className="modal-campo">
              <label htmlFor="modalidad">Modalidad</label>
              <select
                id="modalidad"
                value={modalidad}
                onChange={(e) =>
                  setModalidad(e.target.value as ModalidadEnvio)
                }
              >
                {MODALIDADES.map((opcion) => (
                  <option key={opcion.valor} value={opcion.valor}>
                    {opcion.etiqueta}
                  </option>
                ))}
              </select>
            </div>
            {modalidad === "DOMICILIO" && (
              <div className="modal-campo">
                <label htmlFor="direccionEntrega">Dirección de entrega</label>
                <input
                  id="direccionEntrega"
                  type="text"
                  value={direccionEntrega}
                  onChange={(e) => setDireccionEntrega(e.target.value)}
                  placeholder="Calle, número, referencia"
                  required
                />
              </div>
            )}
          </fieldset>

          <fieldset className="modal-seccion">
            <legend>Paquete</legend>
            <div className="modal-campo">
              <label htmlFor="descripcionPaquete">Descripción</label>
              <input
                id="descripcionPaquete"
                type="text"
                value={descripcionPaquete}
                onChange={(e) => setDescripcionPaquete(e.target.value)}
                maxLength={500}
              />
            </div>
            <div className="modal-grid-4">
              <div className="modal-campo">
                <label htmlFor="pesoKg">Peso (kg)</label>
                <input
                  id="pesoKg"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={pesoKg}
                  onChange={(e) => setPesoKg(e.target.value)}
                  required
                />
              </div>
              <div className="modal-campo">
                <label htmlFor="altoCm">Alto (cm)</label>
                <input
                  id="altoCm"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={altoCm}
                  onChange={(e) => setAltoCm(e.target.value)}
                />
              </div>
              <div className="modal-campo">
                <label htmlFor="anchoCm">Ancho (cm)</label>
                <input
                  id="anchoCm"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={anchoCm}
                  onChange={(e) => setAnchoCm(e.target.value)}
                />
              </div>
              <div className="modal-campo">
                <label htmlFor="largoCm">Largo (cm)</label>
                <input
                  id="largoCm"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={largoCm}
                  onChange={(e) => setLargoCm(e.target.value)}
                />
              </div>
            </div>
          </fieldset>

          <fieldset className="modal-seccion">
            <legend>Destinatario</legend>
            <div className="modal-campo">
              <label htmlFor="destinatarioNombre">Nombre completo</label>
              <input
                id="destinatarioNombre"
                type="text"
                value={destinatarioNombre}
                onChange={(e) => setDestinatarioNombre(e.target.value)}
                maxLength={150}
                required
              />
            </div>
            <div className="modal-grid-2">
              <div className="modal-campo">
                <label htmlFor="destinatarioDocumento">Documento</label>
                <input
                  id="destinatarioDocumento"
                  type="text"
                  value={destinatarioDocumento}
                  onChange={(e) => setDestinatarioDocumento(e.target.value)}
                  maxLength={11}
                  required
                />
              </div>
              <div className="modal-campo">
                <label htmlFor="destinatarioTelefono">Teléfono</label>
                <input
                  id="destinatarioTelefono"
                  type="tel"
                  value={destinatarioTelefono}
                  onChange={(e) => setDestinatarioTelefono(e.target.value)}
                  maxLength={20}
                />
              </div>
            </div>
          </fieldset>

          {cotizando && (
            <p className="modal-cotizacion">Calculando costo...</p>
          )}
          {cotizacionVigente && !cotizando && (
            <div className="modal-cotizacion">
              <p className="modal-cotizacion-total">
                Costo estimado:{" "}
                <strong>
                  {new Intl.NumberFormat("es-PE", {
                    style: "currency",
                    currency: "PEN",
                  }).format(cotizacionVigente.total)}
                </strong>
              </p>
              {cotizacionVigente.pesoFacturableKg >
                cotizacionVigente.pesoKg && (
                <p className="modal-cotizacion-detalle">
                  Tarifa por volumen aplicada:{" "}
                  <strong>{cotizacionVigente.pesoFacturableKg} kg</strong>.
                </p>
              )}
            </div>
          )}

          {error && (
            <div className="modal-error" role="alert">
              {error}
            </div>
          )}

          <footer className="modal-acciones">
            <button
              type="button"
              className="modal-btn-secundario"
              onClick={onCerrar}
              disabled={enviando}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="modal-btn-principal"
              disabled={enviando}
            >
              {enviando ? "Registrando..." : "Registrar envío"}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
}
