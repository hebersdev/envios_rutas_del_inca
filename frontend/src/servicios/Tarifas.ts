import { clienteApi } from "./ClienteApi";
import type { Cotizacion, ModalidadEnvio, Tarifa, TarifaRequest } from "../Tipos/Tarifa";

export function listarTarifas(): Promise<Tarifa[]> {
  return clienteApi<Tarifa[]>("/api/tarifas");
}

export function crearTarifa(datos: TarifaRequest): Promise<Tarifa> {
  return clienteApi<Tarifa>("/api/tarifas", { method: "POST", body: datos });
}

export function editarTarifa(id: number, datos: TarifaRequest): Promise<Tarifa> {
  return clienteApi<Tarifa>(`/api/tarifas/${id}`, { method: "PUT", body: datos });
}

export function cambiarEstadoTarifa(id: number, activo: boolean): Promise<Tarifa> {
  return clienteApi<Tarifa>(`/api/tarifas/${id}/estado`, {
    method: "PATCH",
    body: { activo },
  });
}

export function cotizarEnvio(
  origenId: number,
  destinoId: number,
  pesoKg: number,
  modalidad: ModalidadEnvio,
): Promise<Cotizacion> {
  const parametros = new URLSearchParams({
    origenId: String(origenId),
    destinoId: String(destinoId),
    pesoKg: String(pesoKg),
    modalidad,
  });
  return clienteApi<Cotizacion>(`/api/cotizaciones?${parametros}`);
}
