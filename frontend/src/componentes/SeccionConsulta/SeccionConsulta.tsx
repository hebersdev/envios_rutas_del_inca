import "../SeccionConsulta/SeccionConsulta.css"
import FondoInicioConsulta from "../../imagenes/fondoInicioRutasDelInca.png";
import TarjetaConsulta from "../TarjetaConsulta/TarjetaConsulta";
import { tarjetasConsulta } from "../../datos/tarjetasConsulta";
import BusquedaEnvio from "../BusquedaEnvio/BusquedaEnvio";

function SeccionConsulta() {
    return(
        
        <div className="seccion-consulta">
            <picture>
                <img src= {FondoInicioConsulta} alt= "Fondo de la sección de consulta"/>        
            </picture>
        <div className= "contenedor-consulta">
            <h1>Lo que necesitas, llega con Rutas del Inca</h1>
        <div className="Barra-consulta-busqueda">
            <BusquedaEnvio/>
        </div>
        <div className="contenedor-tarjetas-consulta">
        <div className="tarjetas-consulta">
            {tarjetasConsulta.map((tarjeta) => (
            <TarjetaConsulta
            key={tarjeta.titulo}
            titulo={tarjeta.titulo}
            descripcion={tarjeta.descripcion}
            imagen={tarjeta.imagen}
            />
            ))}
        </div>
        </div>
        </div>
        </div>
    );
}

export default SeccionConsulta;