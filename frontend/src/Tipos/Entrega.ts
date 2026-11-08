import type { EstadoEnvio, ModalidadEnvio } from "./Envio";

export interface Entrega {
  id: number;
  codigoEnvio: string;
  estado: EstadoEnvio;
  modalidad: ModalidadEnvio;
  destinatarioNombre: string;
  destinatarioTelefono: string | null;
  direccionEntrega: string | null;
  fechaLlegada: string | null;
  motorizadoId: number | null;
  motorizadoNombre: string | null;
  sedeDestino: string;
}

export interface MotorizadoReparto {
  id: number;
  nombre: string;
  pendientes: number;
  sede: string;
}

export type VistaEntregas = "COUNTER" | "REPARTO" | "MOTORIZADO";
