import { clienteApi } from "./ClienteApi";
import type { Camion, CamionRequest, EstadoCamion } from "../Tipos/Camion";

export interface FiltrosCamiones {
  estado?: EstadoCamion;
  agenciaActualId?: number;
  activo?: boolean;
}

export function listarCamiones(filtros: FiltrosCamiones = {}): Promise<Camion[]> {
  const parametros = new URLSearchParams();
  if (filtros.estado) parametros.set("estado", filtros.estado);
  if (filtros.agenciaActualId !== undefined) {
    parametros.set("agenciaActualId", String(filtros.agenciaActualId));
  }
  if (filtros.activo !== undefined) parametros.set("activo", String(filtros.activo));
  const consulta = parametros.size > 0 ? `?${parametros}` : "";
  return clienteApi<Camion[]>(`/api/camiones${consulta}`);
}

export function crearCamion(datos: CamionRequest): Promise<Camion> {
  return clienteApi<Camion>("/api/camiones", { method: "POST", body: datos });
}

export function editarCamion(id: number, datos: CamionRequest): Promise<Camion> {
  return clienteApi<Camion>(`/api/camiones/${id}`, { method: "PUT", body: datos });
}

export function cambiarEstadoCamion(id: number, estado: Exclude<EstadoCamion, "EN_VIAJE">): Promise<Camion> {
  return clienteApi<Camion>(`/api/camiones/${id}/estado`, {
    method: "PATCH",
    body: { estado },
  });
}

export function cambiarActivoCamion(id: number, activo: boolean): Promise<Camion> {
  return clienteApi<Camion>(`/api/camiones/${id}/activo`, {
    method: "PATCH",
    body: { activo },
  });
}
