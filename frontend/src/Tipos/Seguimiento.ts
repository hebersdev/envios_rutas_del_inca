import type { EstadoEnvio, ModalidadEnvio } from "./Envio";

export interface EventoSeguimiento {
  estado: EstadoEnvio;
  texto: string;
  fecha: string;
  sede: string | null;
}

export interface Seguimiento {
  codigoEnvio: string;
  estado: EstadoEnvio;
  estadoTexto: string;
  modalidad: ModalidadEnvio;
  origen: string;
  destino: string;
  historial: EventoSeguimiento[];
}

export const TEXTOS_SEGUIMIENTO: Record<EstadoEnvio, string> = {
  REGISTRADO: "Recibido en la agencia de origen",
  ASIGNADO_A_VIAJE: "Listo para salir",
  EN_RUTA: "En camino",
  LLEGO_A_AGENCIA_DESTINO: "Llegó a la agencia de destino",
  LISTO_PARA_RECOJO: "Listo para recoger en agencia",
  EN_REPARTO: "En reparto a domicilio",
  ENTREGADO: "Entregado",
};
