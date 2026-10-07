import "../TarjetaConsulta/TarjetaConsulta.css"

interface TarjetaConsultaProps {
    titulo: string;
    descripcion: string;
    imagen: string;
}

function TarjetaConsulta({
    titulo,
    descripcion,
    imagen
}: TarjetaConsultaProps) {

    return (
        <div className="tarjeta-consulta">
            <h3>{titulo}</h3>
            <img src={imagen} alt={titulo} />
            <p>{descripcion}</p>
        </div>
    );
}

export default TarjetaConsulta;
