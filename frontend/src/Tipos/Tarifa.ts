export type ModalidadEnvio = "AGENCIA" | "DOMICILIO";

export interface TarifaRequest {
  rangoPesoMinKg: number;
  rangoPesoMaxKg: number;
  precioBase: number;
  recargoDomicilio: number;
}

export interface Tarifa extends TarifaRequest {
  id: number;
  activo: boolean;
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