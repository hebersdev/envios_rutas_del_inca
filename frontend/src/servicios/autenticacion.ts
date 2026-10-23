import type { Usuario } from "../Context/ContextoAutenticacion";
import { clienteApi } from "./ClienteApi";

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

export async function iniciarSesion(correo: string, clave: string): Promise<Usuario> {
  const respuesta = await clienteApi<RespuestaAutenticacion>("/api/auth/login", {
    method: "POST",
    body: { correo, clave },
  });
  return respuesta.usuario;
}

export async function obtenerSesion(): Promise<Usuario | null> {
  return await clienteApi<Usuario | null>("/api/auth/sesion") ?? null;
}

export async function registrarUsuario(datos: DatosRegistro): Promise<void> {
  await clienteApi<unknown>("/api/auth/registro", { method: "POST", body: datos });
}

export async function cerrarSesion(): Promise<void> {
  await clienteApi<unknown>("/api/auth/logout", { method: "POST", body: {} });
}