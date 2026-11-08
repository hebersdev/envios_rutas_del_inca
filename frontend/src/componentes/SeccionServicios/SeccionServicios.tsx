import ImagenServicioRegular from "../../imagenes/ImagenServicioRegular.webp"
import ImagenServicioRecojo from "../../imagenes/ImagenServicioRecojo.webp"
import ImagenServicioCarga from "../../imagenes/ImagenServicioCarga.webp"
import ImagenSolucionesLogisticas from "../../imagenes/ImagenSolucionesLogisticas.webp"
import "../SeccionServicios/SeccionServicios.css"

interface Servicio {
    titulo: string;
    descripcion: string;
    imagen: string;
    enlace: string;
}

const servicios: Servicio[] = [
    {
        titulo: "Regular",
        descripcion: "Entrega estándar a nivel nacional, con una solución confiable y eficiente para tus envíos.",
        imagen: ImagenServicioRegular,
        enlace: "/",
    },
    {
        titulo: "Envio a Domicilio",
        descripcion: "Llevamos tus envíos directamente hasta la puerta de tu casa o negocio.",
        imagen: ImagenServicioRecojo,
        enlace: "/servicios/recojo-a-domicilio",
    },
    {
        titulo: "Soluciones Logísticas",
        descripcion: "Nos encargamos de tus envíos para que tú puedas concentrarte en hacer crecer tu negocio.",
        imagen: ImagenSolucionesLogisticas,
        enlace: "/servicios/soluciones-logisticas",
    },
    {
        titulo: "Carga",
        descripcion: "Para paquetes grandes o pesados que requieren mayor capacidad de transporte.",
        imagen: ImagenServicioCarga,
        enlace: "/servicios/carga",
    },
]

function TarjetaServicio({ titulo, descripcion, imagen, enlace }: Servicio) {
    return (
        <article className="tarjeta-servicio">
            <img className="imagen-servicio" src={imagen} alt={titulo} />
            <div className="informacion-servicio">
                <h3 className="titulo-servicio">{titulo}</h3>
                <p className="descripcion-servicio">{descripcion}</p>
                <a className="boton-servicio" href={enlace}>
                    Más información
                </a>
            </div>
        </article>
    );
}

function SeccionServicios() {
    return (
        <section className="seccion-servicios">
            <div className="contenedor-servicios">
                {servicios.map((servicio) => (
                    <TarjetaServicio key={servicio.titulo} 
                    {...servicio} />
                ))}
            </div>
        </section>
    );
}
export default SeccionServicios;
