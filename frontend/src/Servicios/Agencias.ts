import { clienteApi } from "./ClienteApi";
import type { Agencia, AgenciaRequest, AgenciaResumen } from "../Tipos/Agencia";

export function listarAgenciasActivas(): Promise<AgenciaResumen[]> {
  return clienteApi<AgenciaResumen[]>("/api/agencias/activas");
}

export function listarAgencias(activo?: boolean): Promise<Agencia[]> {
  const parametro = activo === undefined ? "" : `?activo=${activo}`;
  return clienteApi<Agencia[]>(`/api/agencias${parametro}`);
}

export function crearAgencia(datos: AgenciaRequest): Promise<Agencia> {
  return clienteApi<Agencia>("/api/agencias", { method: "POST", body: datos });
}

export function editarAgencia(id: number, datos: AgenciaRequest): Promise<Agencia> {
  return clienteApi<Agencia>(`/api/agencias/${id}`, { method: "PUT", body: datos });
}

export function cambiarEstadoAgencia(id: number, activo: boolean): Promise<Agencia> {
  return clienteApi<Agencia>(`/api/agencias/${id}/estado`, {
    method: "PATCH",
    body: { activo },
  });
}
