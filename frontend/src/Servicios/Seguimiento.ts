import { clienteApi } from "./ClienteApi";
import type { Seguimiento } from "../Tipos/Seguimiento";

export function normalizarCodigo(codigo: string): string {
  return codigo.replace(/\s+/g, "").toUpperCase();
}

export function consultarSeguimiento(codigo: string): Promise<Seguimiento> {
  return clienteApi<Seguimiento>(`/api/seguimiento/${encodeURIComponent(normalizarCodigo(codigo))}`);
}
