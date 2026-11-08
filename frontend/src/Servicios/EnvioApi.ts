import { clienteApi, descargarArchivo } from "./ClienteApi";
import type {
  EnvioDetalle,
  EnvioRequest,
  EstadoEnvio,
  PaginaEnvios,
} from "../Tipos/Envio";

export function listarEnvios(
  estado?: EstadoEnvio,
  codigo?: string,
  pagina = 1,
): Promise<PaginaEnvios> {
  const params = new URLSearchParams();
  if (estado) params.set("estado", estado);
  if (codigo) params.set("codigo", codigo);
  params.set("pagina", String(pagina));
  return clienteApi<PaginaEnvios>(`/api/envios?${params.toString()}`);
}

export function obtenerEnvio(id: number): Promise<EnvioDetalle> {
  return clienteApi<EnvioDetalle>(`/api/envios/${id}`);
}

export function registrarEnvio(datos: EnvioRequest): Promise<EnvioDetalle> {
  return clienteApi<EnvioDetalle>("/api/envios", { method: "POST", body: datos });
}

export function cambiarEstadoEnvio(
  id: number,
  estado: EstadoEnvio,
): Promise<EnvioDetalle> {
  return clienteApi<EnvioDetalle>(`/api/envios/${id}/estado`, {
    method: "PATCH",
    body: { estado },
  });
}

export type DocumentoEnvio = "BOLETA" | "ETIQUETA" | "ETIQUETA_PDF";

const DOCUMENTOS: Record<DocumentoEnvio, { ruta: string; prefijo: string; extension: string }> = {
  BOLETA: { ruta: "boleta", prefijo: "boleta", extension: "pdf" },
  ETIQUETA: { ruta: "etiqueta", prefijo: "etiqueta", extension: "png" },
  ETIQUETA_PDF: { ruta: "etiqueta.pdf", prefijo: "etiqueta", extension: "pdf" },
};

export function descargarDocumentoEnvio(id: number, codigo: string, tipo: DocumentoEnvio): Promise<void> {
  const documento = DOCUMENTOS[tipo];
  return descargarArchivo(`/api/envios/${id}/${documento.ruta}`,
    `${documento.prefijo}-${codigo}.${documento.extension}`);
}
