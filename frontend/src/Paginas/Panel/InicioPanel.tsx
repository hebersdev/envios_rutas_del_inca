import { useAutenticacion } from "../../Context/UseAutenticacion";
import "./InicioPanel.css";

export default function InicioPanel() {
  const { usuario } = useAutenticacion();

  return (
    <section className="panel-inicio">
      <p className="panel-inicio-eyebrow">Panel interno</p>
      <h1>Bienvenido, {usuario?.nombre}</h1>
      <p>
        Has iniciado sesión como <strong>{usuario?.rol.replaceAll("_", " ")}</strong>.
      </p>
    </section>
  );
}
