import SeccionConsulta from "../../componentes/SeccionConsulta/SeccionConsulta";
import SeccionExperiencia from "../../componentes/SeccionExperiencia/SeccionExperiencia";
import SeccionServicios from "../../componentes/SeccionServicios/SeccionServicios";
import SeccionEleccion from "../../componentes/SeccionEleccion/SeccionEleccion";
import PieDePagina from "../../componentes/PieDePagina/PieDePagina";
function Home() {
  return (
  <>
    <SeccionConsulta />
    <SeccionExperiencia />
    <SeccionServicios />
    <SeccionEleccion/>
    <PieDePagina/>
  </>
  );
  
}

export default Home;