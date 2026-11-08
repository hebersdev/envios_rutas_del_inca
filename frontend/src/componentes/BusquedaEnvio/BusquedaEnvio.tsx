import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { normalizarCodigo } from "../../Servicios/Seguimiento";
import SearchIcon from "../Iconos/SearchIcon";
import "./BusquedaEnvio.css";

type BusquedaEnvioProps = {
  codigoInicial?: string;
  onBuscar?: (codigo: string) => void;
  titulo?: string;
};

export default function BusquedaEnvio({
  codigoInicial = "",
  onBuscar,
  titulo = "Conoce dónde se encuentra tu envío",
}: BusquedaEnvioProps) {
  const navegar = useNavigate();
  const [codigoSeguimiento, setCodigoSeguimiento] = useState(codigoInicial);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const codigo = normalizarCodigo(codigoSeguimiento);
    if (!codigo) return;

    if (onBuscar) {
      onBuscar(codigo);
      return;
    }

    navegar(`/seguimiento/${encodeURIComponent(codigo)}`);
  }

  return (
    <div className="busqueda-envio">
      <h2>{titulo}</h2>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={codigoSeguimiento}
          onChange={(event) => setCodigoSeguimiento(event.target.value)}
          placeholder="Ingresa tu código de seguimiento..."
          aria-label="Código de seguimiento"
          autoComplete="off"
        />

        <button type="submit" aria-label="Buscar envío">
          <SearchIcon />
        </button>
      </form>

      <button className="boton-ayuda" type="button">
        <span className="info-icon" aria-hidden="true">i</span>
        <span>¿Dónde está mi código de seguimiento?</span>
      </button>
    </div>
  );
}
