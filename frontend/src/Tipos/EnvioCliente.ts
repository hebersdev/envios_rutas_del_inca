import type { EstadoEnvio, ModalidadEnvio } from "./Envio";

export interface EnvioCliente {
  codigoEnvio: string;
  estado: EstadoEnvio;
  modalidad: ModalidadEnvio;
  origen: string;
  destino: string;
  fechaRegistro: string;
  fechaEntrega: string | null;
  costoEnvio: number;
  descripcionPaquete: string | null;
  pesoKg: number;
  destinatarioNombre: string;
  direccionEntrega: string | null;
}

export interface PaginaEnviosCliente {
  contenido: EnvioCliente[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
}
