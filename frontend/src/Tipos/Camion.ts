export type EstadoCamion = "DISPONIBLE" | "EN_VIAJE" | "EN_MANTENIMIENTO";

export interface CamionRequest {
  placa: string;
  pesoMaximoKg: number;
  volumenMaximoM: number;
  agenciaBaseId: number;
}

export interface Camion extends CamionRequest {
  id: number;
  agenciaBaseNombre: string;
  agenciaActualId: number;
  agenciaActualNombre: string;
  estado: EstadoCamion;
  activo: boolean;
}
