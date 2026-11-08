import { clienteApi } from "./ClienteApi";
import type { EnvioPendiente, EstadoViaje, Viaje, ViajeDetalle } from "../Tipos/Viaje";

export function listarViajes(estado?: EstadoViaje): Promise<Viaje[]> {
  return clienteApi<Viaje[]>(`/api/viajes${estado ? `?estado=${estado}` : ""}`);
}

export function obtenerViaje(id: number): Promise<ViajeDetalle> {
  return clienteApi<ViajeDetalle>(`/api/viajes/${id}`);
}

export function listarPendientes(): Promise<EnvioPendiente[]> {
  return clienteApi<EnvioPendiente[]>("/api/viajes/pendientes");
}

export function asignarPendientes(): Promise<{ asignados: number }> {
  return clienteApi<{ asignados: number }>("/api/viajes/asignar-pendientes", { method: "POST" });
}

export type AccionViaje = "despachar" | "llegada" | "finalizar" | "cancelar";

export function actualizarViaje(id: number, accion: AccionViaje): Promise<void> {
  return clienteApi<void>(`/api/viajes/${id}${accion === "cancelar" ? "" : `/${accion}`}`, {
    method: accion === "cancelar" ? "DELETE" : "POST",
  });
}
