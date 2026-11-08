import "../SeccionEleccion/SeccionEleccion.css"
import ImagenCoberturaNacional from "../../imagenes/ImagenCoberturaNacional.webp"
import ImagenFlotaVehicularModerna from "../../imagenes/ImagenFlotaVehicularModerna.webp"
import ImagenServicioPersonalizado from "../../imagenes/ImagenServicioPersonalizado.webp"
import ImagenMonitoreoPermanente from "../../imagenes/ImagenMonitoreoPermanente.webp"

interface Eleccion {
    imagen: string;
    descripcion: string;
}

const elecciones: Eleccion[] = [
    {
        imagen: ImagenCoberturaNacional,
        descripcion: "Cobertura nacional",
    },
    {
        imagen: ImagenServicioPersonalizado,
        descripcion: "Servicio personalizado",
    },
    {
        imagen: ImagenFlotaVehicularModerna,
        descripcion: "Flota vehicular moderna",
    },
    {
        imagen: ImagenMonitoreoPermanente,
        descripcion: "Monitoreo permanente"
    }
]
function TarjetaEleccion({ imagen,descripcion }: Eleccion) {
    return (
        <div className="elemento-eleccion">
            <img className="imagen-eleccion" src={imagen}/>
            <p className="descripcion-eleccion">{descripcion}</p>
        </div>
    );
}

function SeccionEleccion(){
    return (
        <section className="seccion-eleccion">
                <h1>¿Por qué elegirnos?</h1>
            <div className="contenedor-eleccion">
                {elecciones.map((eleccion)=> (
                    <TarjetaEleccion key={eleccion.descripcion}
                    {...eleccion} />
                ))}
            </div>
        </section>
    )
}

export default SeccionEleccion
