import { useState, type FormEvent, type KeyboardEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import ImagenInicioSesion from "../../imagenes/ImagenInicioSesion.png";
import LogoRutasDelInca from "../../imagenes/LogoRutasDelIncaColor.png";
import { useAutenticacion } from "../../context/UseAutenticacion";
import { iniciarSesion, registrarUsuario } from "../../servicios/autenticacion";
import "./Autenticacion.css";

type ModoAutenticacion = "iniciar" | "registrar";
type TipoPersona = "PERSONA_NATURAL" | "EMPRESA";

const opcionesTipoPersona: { valor: TipoPersona; etiqueta: string }[] = [
  { valor: "PERSONA_NATURAL", etiqueta: "Persona Natural" },
  { valor: "EMPRESA", etiqueta: "Empresa" },
];

function Autenticacion() {
  const [modo, setModo] = useState<ModoAutenticacion>("iniciar");
  const [tipoPersona, setTipoPersona] = useState<TipoPersona>("PERSONA_NATURAL");
  const [selectorTipoPersonaAbierto, setSelectorTipoPersonaAbierto] = useState(false);
  const [tipoPersonaActivo, setTipoPersonaActivo] = useState<TipoPersona>("PERSONA_NATURAL");
  const [nombre, setNombre] = useState("");
  const [correo, setCorreo] = useState("");
  const [telefono, setTelefono] = useState("");
  const [dni, setDni] = useState("");
  const [ruc, setRuc] = useState("");
  const [clave, setClave] = useState("");
  const [confirmacion, setConfirmacion] = useState("");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [cargando, setCargando] = useState(false);
  const { iniciarSesion: guardarSesion } = useAutenticacion();
  const navegar = useNavigate();
  const esRegistro = modo === "registrar";
  const formularioListo = esRegistro
    ? nombre.trim().length > 0 &&
      telefono.length > 0 &&
      (tipoPersona === "PERSONA_NATURAL" ? dni.length > 0 : ruc.length > 0) &&
      correo.trim().length > 0 &&
      clave.length > 0 &&
      confirmacion.length > 0
    : correo.trim().length > 0 && clave.length > 0;
  const requisitosClave = [
  {etiqueta: "Mínimo 8 caracteres", cumplido: clave.length >= 8},
  {etiqueta: "Una mayúscula y una minúscula",cumplido: /[A-Z]/.test(clave) && /[a-z]/.test(clave)},
  {etiqueta: "Un número y un carácter especial",cumplido: /[0-9]/.test(clave) && /[^A-Za-z0-9\s]/.test(clave)},
];

  function cambiarModo(nuevoModo: ModoAutenticacion) {
    setModo(nuevoModo);
    setError("");
    setMensaje("");
  }

  function manejarTecladoTipoPersona(evento: KeyboardEvent<HTMLButtonElement>) {
    if (evento.key === "Escape") {
      setSelectorTipoPersonaAbierto(false);
      return;
    }

    if (evento.key === "ArrowDown" || evento.key === "ArrowUp") {
      evento.preventDefault();
      const direccion = evento.key === "ArrowDown" ? 1 : -1;
      setTipoPersonaActivo((actual) => {
        if (!selectorTipoPersonaAbierto) return tipoPersona;
        const indiceActual = opcionesTipoPersona.findIndex((opcion) => opcion.valor === actual);
        const indiceSiguiente = (indiceActual + direccion + opcionesTipoPersona.length) % opcionesTipoPersona.length;
        return opcionesTipoPersona[indiceSiguiente].valor;
      });
      setSelectorTipoPersonaAbierto(true);
      return;
    }

    if (evento.key === "Enter" || evento.key === " ") {
      evento.preventDefault();
      if (selectorTipoPersonaAbierto) setTipoPersona(tipoPersonaActivo);
      else setTipoPersonaActivo(tipoPersona);
      setSelectorTipoPersonaAbierto(!selectorTipoPersonaAbierto);
    }
  }

  async function enviarFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError("");
    setMensaje("");

    if (esRegistro && clave !== confirmacion) {
      setError("Las contraseñas no coinciden.");
      return;
    }

    setCargando(true);

    try {
      if (esRegistro) {
        await registrarUsuario({
          nombre: nombre.trim(),
          correo: correo.trim(),
          telefono: telefono.trim(),
          clave,
          tipoPersona,
          dni: tipoPersona === "PERSONA_NATURAL" ? dni.trim() : null,
          ruc: tipoPersona === "EMPRESA" ? ruc.trim() : null,
        });
        setModo("iniciar");
        setClave("");
        setConfirmacion("");
        setDni("");
        setRuc("");
        setMensaje("Tu cuenta fue creada. Ya puedes iniciar sesión.");
      } else {
        const usuario = await iniciarSesion(correo.trim(), clave);
        guardarSesion(usuario);
        navegar("/inicio");
      }
    } catch (solicitudError) {
      setError(solicitudError instanceof Error ? solicitudError.message : "Ocurrió un error. Inténtalo nuevamente.");
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="autenticacion">
      <section className="autenticacion-presentacion" aria-label="Rutas del Inca: tu carga, siempre en buenas rutas">
        <img src={ImagenInicioSesion} alt="Tu carga, siempre en buenas rutas. Transporte de carga, encomiendas, seguridad y cobertura nacional." />
      </section>

      <section className="autenticacion-panel">
        <div className={`autenticacion-formulario-contenedor${esRegistro ? "" : " autenticacion-formulario-contenedor--iniciar"}`}>
          <div className="autenticacion-marca">
            <img src={LogoRutasDelInca} alt="Rutas del Inca" />
          </div>
          <div className="autenticacion-titulo">
            <h2>{esRegistro ? "Crear cuenta" : "Iniciar sesión"}</h2>
            <p>
              {esRegistro
                ? "Regístrate para rastrear tus envíos, consultar encomiendas y gestionar tus pedidos."
                : "Ingresa a tu cuenta para rastrear tus envíos y gestionar tus pedidos."}
            </p>
          </div>

          <form
            className="autenticacion-formulario"
            onSubmit={enviarFormulario}
          >
            {esRegistro && (
              <>
                <div className="autenticacion-fila">
                  <div className="autenticacion-campo">
                    <label id="tipoPersona-etiqueta">Tipo de registro</label>
                    <div
                      className="autenticacion-selector-tipo"
                      onBlur={(evento) => {
                        if (!evento.currentTarget.contains(evento.relatedTarget)) setSelectorTipoPersonaAbierto(false);
                      }}
                    >
                      <button
                        id="tipoPersona"
                        className="autenticacion-selector-tipo-boton"
                        type="button"
                        role="combobox"
                        aria-labelledby="tipoPersona-etiqueta"
                        aria-haspopup="listbox"
                        aria-expanded={selectorTipoPersonaAbierto}
                        aria-controls="tipoPersona-opciones"
                        aria-activedescendant={selectorTipoPersonaAbierto ? `tipoPersona-opcion-${tipoPersonaActivo}` : undefined}
                        onClick={() => {
                          if (!selectorTipoPersonaAbierto) setTipoPersonaActivo(tipoPersona);
                          setSelectorTipoPersonaAbierto(!selectorTipoPersonaAbierto);
                        }}
                        onKeyDown={manejarTecladoTipoPersona}
                      >
                        <span>{opcionesTipoPersona.find((opcion) => opcion.valor === tipoPersona)?.etiqueta}</span>
                        <span className="autenticacion-selector-tipo-flecha" aria-hidden="true" />
                      </button>
                      {selectorTipoPersonaAbierto && (
                        <div className="autenticacion-selector-tipo-lista" id="tipoPersona-opciones" role="listbox" aria-labelledby="tipoPersona-etiqueta">
                          {opcionesTipoPersona.map((opcion) => (
                            <div
                              id={`tipoPersona-opcion-${opcion.valor}`}
                              className={`autenticacion-selector-tipo-opcion${tipoPersonaActivo === opcion.valor ? " autenticacion-selector-tipo-opcion--activa" : ""}`}
                              key={opcion.valor}
                              role="option"
                              aria-selected={tipoPersona === opcion.valor}
                              onMouseDown={(evento) => evento.preventDefault()}
                              onMouseEnter={() => setTipoPersonaActivo(opcion.valor)}
                              onClick={() => {
                                setTipoPersona(opcion.valor);
                                setTipoPersonaActivo(opcion.valor);
                                setSelectorTipoPersonaAbierto(false);
                              }}
                            >
                              {opcion.etiqueta}
                              <span className="autenticacion-selector-tipo-marca" aria-hidden="true" />
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="autenticacion-campo">
                    <label htmlFor={tipoPersona === "PERSONA_NATURAL" ? "dni" : "ruc"}>
                      {tipoPersona === "PERSONA_NATURAL" ? "DNI (8 dígitos)" : "RUC (11 dígitos)"}
                    </label>
                    {tipoPersona === "PERSONA_NATURAL" ? (
                      <input
                        id="dni"
                        name="dni"
                        type="text"
                        inputMode="numeric"
                        autoComplete="off"
                        maxLength={8}
                        pattern="[0-9]{8}"
                        placeholder="DNI"
                        value={dni}
                        onChange={(evento) => setDni(evento.target.value.replace(/\D/g, ""))}
                        required
                      />
                    ) : (
                      <input
                        id="ruc"
                        name="ruc"
                        type="text"
                        inputMode="numeric"
                        autoComplete="off"
                        maxLength={11}
                        pattern="[0-9]{11}"
                        placeholder="RUC"
                        value={ruc}
                        onChange={(evento) => setRuc(evento.target.value.replace(/\D/g, ""))}
                        required
                      />
                    )}
                  </div>
                </div>

                <div className="autenticacion-campo">
                  <label htmlFor="nombre">{tipoPersona === "EMPRESA" ? "Razón social" : "Nombres y apellidos completos"}</label>
                  <input
                    id="nombre"
                    name="nombre"
                    type="text"
                    autoComplete={tipoPersona === "EMPRESA" ? "organization" : "name"}
                    maxLength={150}
                    placeholder={tipoPersona === "EMPRESA" ? "Razón social" : "Nombres y Apellidos"}
                    value={nombre}
                    onChange={(evento) => setNombre(evento.target.value)}
                    required
                  />
                </div>
              </>
            )}

            <div className="autenticacion-fila">
              {esRegistro && (
                <div className="autenticacion-campo">
                  <label htmlFor="telefono">Celular</label>
                  <input
                  id="telefono"
                  name="telefono"
                  type="tel"
                  inputMode="numeric"
                  maxLength={9}
                  placeholder="Celular"
                  value={telefono}
                  onChange={(e) => setTelefono(e.target.value.replace(/[^0-9]/g, ""))}
                  required
                />
                </div>
              )}
              <div className={`autenticacion-campo${esRegistro ? "" : " autenticacion-campo--completo"}`}>
                <label htmlFor="correo">Correo electrónico</label>
                <input
                  id="correo"
                  name="correo"
                  type="email"
                  autoComplete="email"
                  maxLength={150}
                  placeholder="Correo electrónico"
                  value={correo}
                  onChange={(evento) => setCorreo(evento.target.value)}
                  required
                />
              </div>
            </div>

            <div className="autenticacion-fila">
              <div className={`autenticacion-campo${esRegistro ? "" : " autenticacion-campo--completo"}`}>
                <label htmlFor="clave">Contraseña</label>
                <input
                  id="clave"
                  name="clave"
                  type="password"
                  autoComplete={esRegistro ? "new-password" : "current-password"}
                  minLength={8}
                  maxLength={72}
                  pattern={esRegistro ? "(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s]).{8,72}" : undefined}
                  title={esRegistro ? "Incluye mayúscula, minúscula, número, carácter especial y al menos 8 caracteres." : undefined}
                  placeholder="Contraseña"
                  value={clave}
                  onChange={(evento) => setClave(evento.target.value)}
                  required
                />
              </div>

            {esRegistro && (
              <div className="autenticacion-campo">
                <label htmlFor="confirmacion">Confirmar contraseña</label>
                <input
                  id="confirmacion"
                  name="confirmacion"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  maxLength={72}
                  placeholder="Confirmar contraseña"
                  value={confirmacion}
                  onChange={(evento) => setConfirmacion(evento.target.value)}
                  required
                />
              </div>
            )}
            {esRegistro && (
              
            <ul
              className="autenticacion-requisitos-clave"
              aria-label="Requisitos de la contraseña"
            >
              {requisitosClave.map((requisito) => (
                <li
                  className={
                    requisito.cumplido
                      ? "autenticacion-requisito-clave--cumplido"
                      : ""
                  }
                  key={requisito.etiqueta}
                >
                  <span className="autenticacion-requisito-icono" aria-hidden="true">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="currentColor"
                    >
                      <path d="m9.55 18-5.7-5.7 1.425-1.425 4.275 4.275 9.175-9.175L20.15 7.4z" />
                    </svg>
                  </span>
                
                  {requisito.etiqueta}
                </li>
              ))}
            </ul>

            )}
            </div> 
            
            {error && <p className="autenticacion-alerta autenticacion-alerta--error" role="alert">{error}</p>}
            {mensaje && <p className="autenticacion-alerta autenticacion-alerta--exito" role="status">{mensaje}</p>}

            <button
              className={`autenticacion-enviar${formularioListo ? " autenticacion-enviar--listo" : ""}`}
              type="submit"
              disabled={cargando}
            >
              {cargando ? "Procesando..." : esRegistro ? "Crear cuenta" : "Iniciar sesión"}
            </button>
          </form>

<div className="autenticacion-pie">
  <Link to="/inicio" className="link-volver">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="20"
      height="20"
      viewBox="0 0 48 48"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={4}
      aria-hidden="true"
    >
      <path d="M12.9998 8L6 14L12.9998 21" />
      <path d="M6 14H28.9938C35.8768 14 41.7221 19.6204 41.9904 26.5C42.2739 33.7696 36.2671 40 28.9938 40H11.9984" />
    </svg>

    <span>Volver a Rutas del Inca</span>
  </Link>

  <p>
    <button
      type="button"
      onClick={() =>
        cambiarModo(esRegistro ? "iniciar" : "registrar")
      }
      className="boton-cambiar-modo"
    >
      <span>
        {esRegistro
          ? "Iniciar sesión aquí"
          : "Regístrate aquí"}
      </span>

      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="18"
        height="18"
        viewBox="0 0 24 24"
        fill="currentColor"
        aria-hidden="true"
      >
        <path d="M12 21v-2h7V5h-7V3h7q.825 0 1.413.588T21 5v14q0 .825-.587 1.413T19 21zm-2-4l-1.375-1.45l2.55-2.55H3v-2h8.175l-2.55-2.55L10 7l5 5z" />
      </svg>
    </button>
  </p>
</div>
        </div>
      </section>
    </main>
  );
}

export default Autenticacion;