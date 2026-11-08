import { clienteApi } from "./ClienteApi";
import type { PaginaEnviosCliente } from "../Tipos/EnvioCliente";

export function listarMisEnvios(pagina: number, anteriores: boolean) {
  const parametros = new URLSearchParams({
    pagina: String(pagina),
    anteriores: String(anteriores),
  });
  return clienteApi<PaginaEnviosCliente>(`/api/mis-envios?${parametros}`);
}
