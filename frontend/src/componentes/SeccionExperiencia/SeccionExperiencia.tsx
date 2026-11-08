import FondoExperienciaRutasDelInca from "../../imagenes/FondoExperienciaRutasDelInca.webp";
import "./SeccionExperiencia.css";
import useContador from "./UserContador";

interface Estadistica {
    meta: number;
    prefijo: string;
    descripcion: string;
}

const estadisticas: Estadistica[] = [
    {
        meta: 3,
        prefijo: "+",
        descripcion: "Años liderando la logística en el Perú",
    },
    {
        meta: 150,
        prefijo: "+",
        descripcion: "Tiendas y agentes a nivel nacional",
    },
    {
        meta: 600,
        prefijo: "+",
        descripcion: "Destinos activos a nivel nacional",
    },
    {
        meta: 500,
        prefijo: "+",
        descripcion: "Emprendedores confían en nosotros",
    },
];

function ItemEstadistica({ meta, prefijo, descripcion }: Estadistica) {
    const { referencia, valor } = useContador({ meta });

    return (
        <div className="estadistica-experiencia">
            <span className="numero-experiencia" ref={referencia}>
                {prefijo}
                {valor}
            </span>

            <p className="descripcion-experiencia">{descripcion}</p>
        </div>
    );
}

function SeccionExperiencia() {
    return (
        <section className="seccion-experiencia">
            <div className="contenedor-experiencia">
                <h2>Especialistas en transporte de carga</h2>
                <picture className="imagen-experiencia">
                    <img
                        src={FondoExperienciaRutasDelInca}
                        alt="Experiencia de Rutas del Inca"
                    />
                </picture>

                <div className="contenedor-informacion-experiencia">
                    <div className="estadisticas-experiencia">
                        {estadisticas.map((estadistica) => (
                            <ItemEstadistica
                                key={estadistica.descripcion}
                                {...estadistica}
                            />
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
}

export default SeccionExperiencia;
