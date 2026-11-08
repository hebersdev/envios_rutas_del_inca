import { useState } from "react";
import { descargarDocumentoEnvio, type DocumentoEnvio } from "../../Servicios/EnvioApi";
import "./DescargasEnvio.css";

const opciones: { tipo: DocumentoEnvio; etiqueta: string }[] = [
  { tipo: "BOLETA", etiqueta: "Descargar boleta" },
  { tipo: "ETIQUETA_PDF", etiqueta: "Descargar etiqueta" },
];

export default function DescargasEnvio({ envioId, codigo }: { envioId: number; codigo: string }) {
  const [descargando, setDescargando] = useState<DocumentoEnvio | null>(null);
  const [error, setError] = useState("");

  async function descargar(tipo: DocumentoEnvio) {
    setDescargando(tipo);
    setError("");
    try {
      await descargarDocumentoEnvio(envioId, codigo, tipo);
    } catch (fallo: unknown) {
      setError(fallo instanceof Error ? fallo.message : "No se pudo descargar el documento. Inténtalo nuevamente.");
    } finally {
      setDescargando(null);
    }
  }

  return (
    <section className="envio-descargas" aria-label="Documentos del envío">
      <div className="envio-documentos">
        {opciones.map(({ tipo, etiqueta }) => (
          <button key={tipo} type="button" disabled={descargando !== null}
            onClick={() => void descargar(tipo)}>
            {descargando === tipo ? "Preparando descarga..." : etiqueta}
          </button>
        ))}
      </div>
      <p>Boleta de 80 mm · Etiqueta de 100 × 90 mm. Imprime al 100 % del tamaño.</p>
      {error && <p className="envio-error" role="alert">{error}</p>}
    </section>
  );
}
