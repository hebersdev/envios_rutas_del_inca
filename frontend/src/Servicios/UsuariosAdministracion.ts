import { clienteApi } from "./ClienteApi";
import type {
  ActualizarRolAdministracion,
  CrearUsuarioAdministracion,
  UsuarioAdministracion,
} from "../Tipos/UsuarioAdministracion";

export function listarUsuariosAdministracion(): Promise<UsuarioAdministracion[]> {
  return clienteApi<UsuarioAdministracion[]>("/api/usuarios");
}

export function crearUsuarioAdministracion(
  datos: CrearUsuarioAdministracion,
): Promise<UsuarioAdministracion> {
  return clienteApi<UsuarioAdministracion>("/api/usuarios", {
    method: "POST",
    body: datos,
  });
}

export function actualizarRolUsuario(
  id: number,
  datos: ActualizarRolAdministracion,
): Promise<UsuarioAdministracion> {
  return clienteApi<UsuarioAdministracion>(`/api/usuarios/${id}/rol`, {
    method: "PATCH",
    body: datos,
  });
}

export function eliminarUsuarioAdministracion(id: number): Promise<void> {
  return clienteApi<void>(`/api/usuarios/${id}`, { method: "DELETE" });
}