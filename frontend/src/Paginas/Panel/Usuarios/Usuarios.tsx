import { useEffect, useState, type FormEvent } from "react";
import DataTable, { type TableColumn } from "react-data-table-component";
import type { Agencia } from "../../../Tipos/Agencia";
import type {
  ActualizarRolAdministracion,
  CrearUsuarioAdministracion,
  RolInternoUsuario,
  UsuarioAdministracion,
} from "../../../Tipos/UsuarioAdministracion";
import {
  actualizarRolUsuario,
  crearUsuarioAdministracion,
  eliminarUsuarioAdministracion,
  listarUsuariosAdministracion,
} from "../../../Servicios/UsuariosAdministracion";
import { listarAgencias } from "../../../Servicios/Agencias";
import "./Usuarios.css";

const roles: { valor: RolInternoUsuario; etiqueta: string }[] = [
  { valor: "ADMINISTRADOR", etiqueta: "Administrador" },
  { valor: "ENCARGADO_LOGISTICA", etiqueta: "Encargado de logística" },
  { valor: "COUNTER", etiqueta: "Counter" },
  { valor: "MOTORIZADO", etiqueta: "Motorizado" },
];

function etiquetaRol(rol: RolInternoUsuario): string {
  return roles.find((opcion) => opcion.valor === rol)?.etiqueta ?? rol;
}

function formularioInicial() {
  return { nombre: "", correo: "", clave: "", rol: "" as RolInternoUsuario | "", agenciaId: "" };
}

const estilosTabla = {
  headRow: { style: { backgroundColor: "#f8f9fa", minHeight: "48px" } },
  headCells: { style: { color: "#626b73", fontSize: "12px", fontWeight: 700 } },
  rows: { style: { minHeight: "56px", fontSize: "14px" } },
};

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState<UsuarioAdministracion[]>([]);
  const [agencias, setAgencias] = useState<Agencia[]>([]);
  const [formulario, setFormulario] = useState(formularioInicial);
  const [usuarioEditando, setUsuarioEditando] = useState<UsuarioAdministracion | null>(null);
  const [rolEditado, setRolEditado] = useState<RolInternoUsuario>("COUNTER");
  const [agenciaEditada, setAgenciaEditada] = useState("");
  const [busqueda, setBusqueda] = useState("");
  const [recarga, setRecarga] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");

  useEffect(() => {
    let vigente = true;
    Promise.all([listarUsuariosAdministracion(), listarAgencias(true)])
      .then(([listaUsuarios, listaAgencias]) => {
        if (!vigente) return;
        setUsuarios(listaUsuarios);
        setAgencias(listaAgencias);
        setError("");
      })
      .catch((errorSolicitud: unknown) => {
        if (vigente) {
          setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudieron cargar los usuarios.");
        }
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
  }, [recarga]);

  useEffect(() => {
    if (!aviso) return;
    const temporizador = window.setTimeout(() => setAviso(""), 3500);
    return () => window.clearTimeout(temporizador);
  }, [aviso]);

  const usuariosFiltrados = usuarios.filter((usuario) => (
    `${usuario.nombre} ${usuario.correo} ${usuario.sede} ${etiquetaRol(usuario.rol)}`
      .toLocaleLowerCase()
      .includes(busqueda.trim().toLocaleLowerCase())
  ));

  function recargar() {
    setCargando(true);
    setRecarga((actual) => actual + 1);
  }

  async function crearUsuario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!formulario.rol) return;

    const datos: CrearUsuarioAdministracion = {
      nombre: formulario.nombre.trim(),
      correo: formulario.correo.trim(),
      clave: formulario.clave,
      rol: formulario.rol,
      agenciaId: formulario.rol === "ADMINISTRADOR" ? null : Number(formulario.agenciaId),
    };

    setGuardando(true);
    setError("");
    try {
      await crearUsuarioAdministracion(datos);
      setFormulario(formularioInicial());
      setAviso("El usuario se creó correctamente.");
      recargar();
    } catch (errorSolicitud) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo crear el usuario.");
    } finally {
      setGuardando(false);
    }
  }

  function abrirEdicion(usuario: UsuarioAdministracion) {
    setUsuarioEditando(usuario);
    setRolEditado(usuario.rol);
    setAgenciaEditada(usuario.agenciaId?.toString() ?? "");
  }

  async function guardarRol(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!usuarioEditando) return;

    const datos: ActualizarRolAdministracion = {
      rol: rolEditado,
      agenciaId: rolEditado === "ADMINISTRADOR" ? null : Number(agenciaEditada),
    };

    setGuardando(true);
    setError("");
    try {
      await actualizarRolUsuario(usuarioEditando.id, datos);
      setUsuarioEditando(null);
      setAviso("El rol se actualizó correctamente.");
      recargar();
    } catch (errorSolicitud) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo actualizar el rol.");
    } finally {
      setGuardando(false);
    }
  }

  async function eliminarUsuario(usuario: UsuarioAdministracion) {
    if (!window.confirm(`¿Desactivar la cuenta de ${usuario.nombre}?`)) return;

    setGuardando(true);
    setError("");
    try {
      await eliminarUsuarioAdministracion(usuario.id);
      setAviso("La cuenta se desactivó correctamente.");
      recargar();
    } catch (errorSolicitud) {
      setError(errorSolicitud instanceof Error ? errorSolicitud.message : "No se pudo desactivar el usuario.");
    } finally {
      setGuardando(false);
    }
  }

  const columnas: TableColumn<UsuarioAdministracion>[] = [
    { name: "Nombre", selector: (usuario) => usuario.nombre, sortable: true, grow: 1.1 },
    { name: "Correo", selector: (usuario) => usuario.correo, sortable: true, grow: 1.4 },
    { name: "Sede", selector: (usuario) => usuario.sede, sortable: true },
    { name: "Rol", selector: (usuario) => etiquetaRol(usuario.rol), sortable: true },
    {
      name: "Acciones",
      cell: (usuario) => (
        <div className="usuarios-acciones">
          <button type="button" onClick={() => abrirEdicion(usuario)} disabled={guardando}>
            Editar
          </button>
          <button type="button" onClick={() => void eliminarUsuario(usuario)} disabled={guardando}>
            Eliminar
          </button>
        </div>
      ),
      grow: 1.3,
    },
  ];

  return (
    <section className="usuarios-pagina">
      <header className="usuarios-encabezado">
        <div>
          <p className="usuarios-eyebrow">Administración</p>
          <h1>Usuarios</h1>
          <p>Crea cuentas internas y administra sus roles y sedes.</p>
        </div>
      </header>

      {error && <p className="usuarios-error" role="alert">{error}</p>}

      <div className="usuarios-contenido">
        <form className="usuarios-formulario" onSubmit={crearUsuario}>
          <h2>Crear usuario</h2>
          <label>
            Nombre
            <input
              type="text"
              value={formulario.nombre}
              onChange={(evento) => setFormulario({ ...formulario, nombre: evento.target.value })}
              maxLength={150}
              autoComplete="name"
              required
            />
          </label>
          <label>
            Correo
            <input
              type="email"
              value={formulario.correo}
              onChange={(evento) => setFormulario({ ...formulario, correo: evento.target.value })}
              maxLength={150}
              autoComplete="email"
              required
            />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              value={formulario.clave}
              onChange={(evento) => setFormulario({ ...formulario, clave: evento.target.value })}
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              required
            />
          </label>
          <label>
            Rol
            <select
              value={formulario.rol}
              onChange={(evento) => setFormulario({ ...formulario, rol: evento.target.value as RolInternoUsuario | "" })}
              required
            >
              <option value="" disabled>Selecciona un rol</option>
              {roles.map((rol) => <option key={rol.valor} value={rol.valor}>{rol.etiqueta}</option>)}
            </select>
          </label>
          {formulario.rol !== "ADMINISTRADOR" ? (
            <label>
              Sede
              <select
                value={formulario.agenciaId}
                onChange={(evento) => setFormulario({ ...formulario, agenciaId: evento.target.value })}
                required={formulario.rol !== ""}
                disabled={formulario.rol === ""}
              >
                <option value="">Selecciona una sede</option>
                {agencias.map((agencia) => (
                  <option key={agencia.id} value={agencia.id}>{agencia.nombre}</option>
                ))}
              </select>
            </label>
          ) : (
            <p className="usuarios-sin-sede">Los administradores no requieren sede.</p>
          )}
          <button className="usuarios-boton-principal" type="submit" disabled={guardando || cargando}>
            {guardando ? "Guardando..." : "Crear usuario"}
          </button>
        </form>

        <div className="usuarios-tabla">
          <DataTable
            columns={columnas}
            data={usuariosFiltrados}
            keyField="id"
            customStyles={estilosTabla}
            pagination
            paginationPerPage={8}
            paginationRowsPerPageOptions={[8, 16, 32]}
            highlightOnHover
            responsive
            progressPending={cargando}
            progressComponent={<p className="usuarios-estado">Cargando usuarios...</p>}
            noDataComponent={<p className="usuarios-estado">No hay usuarios que coincidan.</p>}
            subHeader={(
              <input
                className="usuarios-busqueda"
                type="search"
                aria-label="Buscar usuarios"
                placeholder="Buscar nombre, correo, sede o rol"
                value={busqueda}
                onChange={(evento) => setBusqueda(evento.target.value)}
              />
            )}
          />
        </div>
      </div>

      {usuarioEditando && (
        <div className="usuarios-modal-fondo">
          <section className="usuarios-modal" role="dialog" aria-modal="true" aria-labelledby="usuarios-modal-titulo">
            <h2 id="usuarios-modal-titulo">Actualizar rol</h2>
            <p>{usuarioEditando.nombre} · {usuarioEditando.correo}</p>
            <form onSubmit={guardarRol}>
              <label>
                Rol
                <select value={rolEditado} onChange={(evento) => setRolEditado(evento.target.value as RolInternoUsuario)}>
                  {roles.map((rol) => <option key={rol.valor} value={rol.valor}>{rol.etiqueta}</option>)}
                </select>
              </label>
              {rolEditado !== "ADMINISTRADOR" && (
                <label>
                  Sede
                  <select value={agenciaEditada} onChange={(evento) => setAgenciaEditada(evento.target.value)} required>
                    <option value="">Selecciona una sede</option>
                    {agencias.map((agencia) => (
                      <option key={agencia.id} value={agencia.id}>{agencia.nombre}</option>
                    ))}
                  </select>
                </label>
              )}
              <div className="usuarios-modal-acciones">
                <button type="button" onClick={() => setUsuarioEditando(null)} disabled={guardando}>Cancelar</button>
                <button className="usuarios-boton-principal" type="submit" disabled={guardando}>
                  {guardando ? "Guardando..." : "Guardar cambios"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </section>
  );
}