import LogoRutasDelInca from '../../imagenes/logoRutasDelInca.png';
import ImagenLibroDeReclamaciones from '../../imagenes/ImagenLibroDeReclamaciones.png';
import './PieDePagina.css';

function PieDePagina() {
    return (
        <footer className="pie-pagina">
            <div className="pie-pagina__contenido">
                <a className="pie-pagina__marca" href="#inicio" aria-label="Rutas del Inca, inicio">
                    <img src={LogoRutasDelInca} alt="Rutas del Inca" />
                </a>

                <nav className="pie-pagina__columna" aria-label="Consultas">
                    <h2>Consultas</h2>
                    <a href="#preguntas-frecuentes">Preguntas frecuentes</a>
                    <a href="#terminos-y-condiciones">Términos y condiciones</a>
                    <a href="#politicas-de-privacidad">Políticas de privacidad</a>
                    <a href="#buzon-de-sugerencias">Buzón de sugerencias</a>
                    <a href="#directorio">Directorio</a>
                </nav>

                <section className="pie-pagina__columna pie-pagina__direcciones" aria-labelledby="pie-direcciones">
                    <h2 id="pie-direcciones">Direcciones</h2>
                    <div>
                        <h3>Arequipa</h3>
                        <p>Av. Italia N.° 420, Cerro Colorado, Arequipa</p>
                    </div>
                    <div>
                        <h3>Lima</h3>
                        <p>Jr. Luna Pizarro 430, La Victoria</p>
                    </div>
                </section>

                <section className="pie-pagina__columna pie-pagina__redes" aria-labelledby="pie-redes">
                    <h2 id="pie-redes">Redes sociales</h2>
                    <ul className="pie-pagina__lista-redes">
                        <li><svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"><title>facebook</title><path fill="currentColor" d="M20.9 2H3.1A1.1 1.1 0 0 0 2 3.1v17.8A1.1 1.1 0 0 0 3.1 22h9.58v-7.75h-2.6v-3h2.6V9a3.64 3.64 0 0 1 3.88-4a20 20 0 0 1 2.33.12v2.7H17.3c-1.26 0-1.5.6-1.5 1.47v1.93h3l-.39 3H15.8V22h5.1a1.1 1.1 0 0 0 1.1-1.1V3.1A1.1 1.1 0 0 0 20.9 2"/></svg></li>
                        <li><svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"><title>instagram</title><path fill="currentColor" d="M7.8 2h8.4C19.4 2 22 4.6 22 7.8v8.4a5.8 5.8 0 0 1-5.8 5.8H7.8C4.6 22 2 19.4 2 16.2V7.8A5.8 5.8 0 0 1 7.8 2m-.2 2A3.6 3.6 0 0 0 4 7.6v8.8C4 18.39 5.61 20 7.6 20h8.8a3.6 3.6 0 0 0 3.6-3.6V7.6C20 5.61 18.39 4 16.4 4zm9.65 1.5a1.25 1.25 0 0 1 1.25 1.25A1.25 1.25 0 0 1 17.25 8A1.25 1.25 0 0 1 16 6.75a1.25 1.25 0 0 1 1.25-1.25M12 7a5 5 0 0 1 5 5a5 5 0 0 1-5 5a5 5 0 0 1-5-5a5 5 0 0 1 5-5m0 2a3 3 0 0 0-3 3a3 3 0 0 0 3 3a3 3 0 0 0 3-3a3 3 0 0 0-3-3"/></svg></li>
                        <li><svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"><title>tiktok-alt</title><path fill="currentColor" d="M18.857 3H5.143A2.145 2.145 0 0 0 3 5.143v13.714C3 20.04 3.961 21 5.143 21h13.714A2.145 2.145 0 0 0 21 18.857V5.143A2.145 2.145 0 0 0 18.857 3m-1.712 7.853a3.17 3.17 0 0 1-1.822-.371a3.2 3.2 0 0 1-1.16-1.066v4.944a3.654 3.654 0 1 1-3.654-3.654c.076 0 .15.007.225.011v1.801c-.075-.009-.148-.023-.225-.023a1.865 1.865 0 1 0 0 3.73c1.03 0 1.94-.811 1.94-1.841l.018-8.398h1.723a3.21 3.21 0 0 0 2.957 2.865v2.002"/></svg></li>          
                        <li><svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"><title>linkedin</title><path fill="currentColor" d="M19 3a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm-.5 15.5v-5.3a3.26 3.26 0 0 0-3.26-3.26c-.85 0-1.84.52-2.32 1.3v-1.11h-2.79v8.37h2.79v-4.93c0-.77.62-1.4 1.39-1.4a1.4 1.4 0 0 1 1.4 1.4v4.93zM6.88 8.56a1.68 1.68 0 0 0 1.68-1.68c0-.93-.75-1.69-1.68-1.69a1.69 1.69 0 0 0-1.69 1.69c0 .93.76 1.68 1.69 1.68m1.39 9.94v-8.37H5.5v8.37z"/></svg></li>
                    </ul>
                    <a className="pie-pagina__reclamos" href="#libro-de-reclamaciones" aria-label="Libro de reclamaciones">
                        <img src={ImagenLibroDeReclamaciones} alt="Imagen libro de reclamaciones" />
                    </a>
                </section>
            </div>


        </footer>
    );
}

export default PieDePagina;