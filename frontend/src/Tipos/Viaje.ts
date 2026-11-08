export type EstadoViaje = "PROGRAMADO" | "EN_RUTA" | "LLEGADO" | "FINALIZADO";

export interface CargaEnvio {
  id: number;
  codigoEnvio: string;
  destinoId: number;
  destino: string;
  pesoKg: number;
  altoCm: number | null;
  anchoCm: number | null;
  largoCm: number | null;
}

export interface Viaje {
  id: number;
  camionId: number;
  placa: string;
  agenciaOrigenId: number;
  origen: string;
  agenciaDestinoId: number;
  destino: string;
  estado: EstadoViaje;
  pesoMaximoKg: number;
  volumenMaximoM: number;
  pesoCargadoKg: number;
  volumenCargadoM: number;
  cantidadEnvios: number;
  sinMedidas: boolean;
  fechaSalida: string | null;
  fechaLlegada: string | null;
}

export interface ViajeDetalle {
  viaje: Viaje;
  envios: CargaEnvio[];
}

export interface EnvioPendiente {
  envio: CargaEnvio;
  motivo: string;
}
