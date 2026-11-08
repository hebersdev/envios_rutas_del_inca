import { descargarArchivo } from "./ClienteApi";

export function descargarReportes(desde: string, hasta: string): Promise<void> {
  const parametros = new URLSearchParams({ desde, hasta });
  return descargarArchivo(
    `/api/reportes/excel?${parametros.toString()}`,
    `reportes-${desde}-${hasta}.xlsx`,
  );
}
