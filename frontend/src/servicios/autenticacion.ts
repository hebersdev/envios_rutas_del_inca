import type { Usuario } from "../context/ContextoAutenticacion";

interface RespuestaAutenticacion {
  usuario: Usuario;
}

interface DatosRegistro {
  nombre: string;
  correo: string;
  telefono: string;
  clave: string;
  tipoPersona: "PERSONA_NATURAL" | "EMPRESA";
  dni: string | null;
  ruc: string | null;
}

const rutaAutenticacion = `${import.meta.env.VITE_API_URL ?? ""}/api/auth`;

async function enviarSolicitud<T>(ruta: string, datos: unknown): Promise<T> {
  let respuesta: Response;

  try {
    respuesta = await fetch(`${rutaAutenticacion}${ruta}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(datos),
    });
  } catch {
    throw new Error("No fue posible conectar con el servidor. Inténtalo nuevamente.");
  }

  const contenido = await respuesta.json().catch(() => null) as {
    mensaje?: string;
    error?: string;
  } | null;

  if (!respuesta.ok) {
    throw new Error(contenido?.mensaje ?? contenido?.error ?? "No se pudo completar la solicitud.");
  }

  return contenido as T;
}

export async function iniciarSesion(correo: string, clave: string): Promise<Usuario> {
  const respuesta = await enviarSolicitud<RespuestaAutenticacion>("/login", { correo, clave });
  return respuesta.usuario;
}

export async function registrarUsuario(datos: DatosRegistro): Promise<void> {
  await enviarSolicitud<unknown>("/registro", datos);
}

export async function cerrarSesion(): Promise<void> {
  await enviarSolicitud<unknown>("/logout", {});
}