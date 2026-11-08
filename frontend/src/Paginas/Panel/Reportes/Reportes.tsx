import { useState, type FormEvent } from "react";
import { descargarReportes } from "../../../Servicios/Reportes";
import "./Reportes.css";

const secciones = [
  { nombre: "Envíos por período", detalle: "Busca cada código y filtra por fecha, estado, modalidad o destino." },
  { nombre: "Ingresos", detalle: "Costo por envío, con fecha y mes para analizar los importes." },
  { nombre: "Viajes y camiones", detalle: "Envíos por viaje, con peso, volumen y capacidad del camión." },
  { nombre: "Entregas", detalle: "Estado y tiempo de entrega de cada envío con destino en tu sede." },
  { nombre: "Motorizados", detalle: "Envíos entregados por cada motorizado y fecha de entrega." },
];

function fechaLocal(fecha: Date): string {
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, "0");
  const dia = String(fecha.getDate()).padStart(2, "0");
  return `${anio}-${mes}-${dia}`;
}

export default function Reportes() {
  const hoy = new Date();
  const [desde, setDesde] = useState(fechaLocal(new Date(hoy.getFullYear(), hoy.getMonth(), 1)));
  const [hasta, setHasta] = useState(fechaLocal(hoy));
  const [descargando, setDescargando] = useState(false);
  const [error, setError] = useState("");

  async function descargar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError("");
    if (hasta < desde) {
      setError("La fecha final debe ser posterior o igual a la inicial.");
      return;
    }
    if (Date.parse(hasta) - Date.parse(desde) > 365 * 24 * 60 * 60 * 1000) {
      setError("El período no puede superar 366 días.");
      return;
    }
    setDescargando(true);
    try {
      await descargarReportes(desde, hasta);
    } catch (fallo: unknown) {
      setError(fallo instanceof Error ? fallo.message : "No se pudo descargar el reporte.");
    } finally {
      setDescargando(false);
    }
  }

  return (
    <section className="reportes-pagina">
      <header className="reportes-encabezado">
        <p>Coordinación de logística</p>
        <h1>Reportes de mi sede</h1>
        <p>Descarga un archivo Excel con cinco hojas y los datos de tu sede.</p>
      </header>

      <form className="reportes-formulario" onSubmit={(evento) => void descargar(evento)}>
        <div className="reportes-fechas">
          <label>
            Desde
            <input type="date" required value={desde} onChange={(evento) => setDesde(evento.target.value)} />
          </label>
          <label>
            Hasta
            <input type="date" required value={hasta} onChange={(evento) => setHasta(evento.target.value)} />
          </label>
          <button type="submit" disabled={descargando}>
            {descargando ? "Preparando archivo..." : "Descargar Excel"}
          </button>
        </div>
        <p className="reportes-ayuda">El período puede abarcar hasta 366 días.</p>
        {error && <p className="reportes-error" role="alert">{error}</p>}
      </form>

      <div className="reportes-secciones">
        {secciones.map((seccion, indice) => (
          <article key={seccion.nombre}>
            <span>{String(indice + 1).padStart(2, "0")}</span>
            <div>
              <h2>{seccion.nombre}</h2>
              <p>{seccion.detalle}</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
