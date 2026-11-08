export type EstadoEnvio =
  | "REGISTRADO"
  | "ASIGNADO_A_VIAJE"
  | "EN_RUTA"
  | "LLEGO_A_AGENCIA_DESTINO"
  | "LISTO_PARA_RECOJO"
  | "EN_REPARTO"
  | "ENTREGADO";

export type ModalidadEnvio = "AGENCIA" | "DOMICILIO";

export const ETIQUETAS_ESTADO: Record<EstadoEnvio, string> = {
  REGISTRADO: "Registrado",
  ASIGNADO_A_VIAJE: "Listo para salir",
  EN_RUTA: "En ruta",
  LLEGO_A_AGENCIA_DESTINO: "Llegó a sede destino",
  LISTO_PARA_RECOJO: "Listo para recoger",
  EN_REPARTO: "En reparto",
  ENTREGADO: "Entregado",
};

export interface EnvioResumen {
  id: number;
  codigoEnvio: string;
  estado: EstadoEnvio;
  modalidad: ModalidadEnvio;
  pesoKg: number;
  costoEnvio: number;
  fechaRegistro: string;
  remitenteNombre: string;
  destinoNombre: string;
}

export interface EnvioRemitente {
  id: number | null;
  nombre: string;
  documento: string;
  telefono: string | null;
}

export interface EnvioSede {
  id: number;
  nombre: string;
  ciudad: string;
  direccion: string;
}

export interface EnvioDetalle {
  id: number;
  codigoEnvio: string;
  estado: EstadoEnvio;
  modalidad: ModalidadEnvio;
  remitente: EnvioRemitente;
  origen: EnvioSede;
  destino: EnvioSede;
  descripcionPaquete: string;
  pesoKg: number;
  altoCm: number | null;
  anchoCm: number | null;
  largoCm: number | null;
  pesoVolumetricoKg: number;
  pesoFacturableKg: number;
  destinatarioNombre: string;
  destinatarioDocumento: string;
  destinatarioTelefono: string;
  direccionEntrega: string | null;
  costoEnvio: number;
  fechaRegistro: string;
  puedeCambiarEstado: boolean;
}

export interface EnvioRequest {
  clienteId: number | null;
  agenciaDestinoId: number;
  modalidad: ModalidadEnvio;
  descripcionPaquete: string;
  pesoKg: number;
  altoCm: number | null;
  anchoCm: number | null;
  largoCm: number | null;
  remitenteNombre: string;
  remitenteDocumento: string;
  remitenteTelefono: string | null;
  destinatarioNombre: string;
  destinatarioDocumento: string;
  destinatarioTelefono: string;
  direccionEntrega?: string | null;
}

export interface Cotizacion {
  origenId: number;
  destinoId: number;
  pesoKg: number;
  pesoVolumetricoKg: number;
  pesoFacturableKg: number;
  modalidad: ModalidadEnvio;
  precioBase: number;
  recargo: number;
  total: number;
  rangoPesoMinKg: number;
  rangoPesoMaxKg: number;
}

export interface PaginaEnvios {
  contenido: EnvioResumen[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
}
