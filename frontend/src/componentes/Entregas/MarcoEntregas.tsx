import type { ReactNode } from "react";
import "./MarcoEntregas.css";

interface Props {
  titulo: string;
  descripcion: string;
  cargando: boolean;
  error: string;
  errorAccion: string;
  mensaje: string;
  actualizar: () => void;
  ocupado: boolean;
  children: ReactNode;
}

export default function MarcoEntregas({
  titulo,
  descripcion,
  cargando,
  error,
  errorAccion,
  mensaje,
  actualizar,
  ocupado,
  children,
}: Props) {
  return (
    <section className="entregas-pagina" aria-busy={cargando || ocupado}>
      <header className="entregas-cabecera">
        <div>
          <h1>{titulo}</h1>
          <p>{descripcion}</p>
        </div>
        <button type="button" onClick={actualizar} disabled={cargando || ocupado}>
          Actualizar
        </button>
      </header>

      {mensaje && (
        <p className="entregas-mensaje" role="status">
          {mensaje}
        </p>
      )}

      {errorAccion && (
        <div className="entregas-error" role="alert">
          <p>{errorAccion}</p>
          <button type="button" disabled={ocupado} onClick={actualizar}>
            Actualizar lista
          </button>
        </div>
      )}

      {cargando ? (
        <p role="status">Cargando envíos...</p>
      ) : error ? (
        <div className="entregas-error" role="alert">
          <p>{error}</p>
          <button type="button" onClick={actualizar}>
            Reintentar
          </button>
        </div>
      ) : (
        children
      )}
    </section>
  );
}
