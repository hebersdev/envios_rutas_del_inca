export interface Agencia {
  id: number;
  nombre: string;
  direccion: string;
  ciudad: string;
  departamento: string;
  activo: boolean;
}

export interface AgenciaRequest {
  nombre: string;
  direccion: string;
  ciudad: string;
  departamento: string;
}

export interface AgenciaResumen {
  id: number;
  nombre: string;
  ciudad: string;
  departamento: string;
}
