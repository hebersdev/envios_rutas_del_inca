import { useState } from "react";
import type { FormEvent } from "react";
import "../BusquedaEnvio/BusquedaEnvio.css";
import SearchIcon from "../iconos/SearchIcon";

function BusquedaEnvio() {
    const [codigoSeguimiento, setCodigoSeguimiento] = useState("");

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        const codigo = codigoSeguimiento.trim();

        if (!codigo) {
            return;
        }

        console.log("Código de seguimiento:", codigo);
    };

    return (
        <div className="busqueda-envio">
            <h2>Conoce dónde se encuentra tu envío</h2>

            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    value={codigoSeguimiento}
                    onChange={(event) =>
                        setCodigoSeguimiento(event.target.value)
                    }
                    placeholder="Ingresa tu código de seguimiento..."
                    aria-label="Código de seguimiento"
                    autoComplete="off"
                />

                <button type="submit" aria-label="Buscar envío">
                    <SearchIcon />
                </button>
            </form>

            <button className="boton-ayuda" type="button">
                <span className="info-icon" aria-hidden="true">
                    i
                </span>
                <span>¿Dónde está mi código de seguimiento?</span>
            </button>
        </div>
    );
}

export default BusquedaEnvio;
