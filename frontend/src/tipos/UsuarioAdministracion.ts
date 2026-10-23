export type RolInternoUsuario =
  | "ADMINISTRADOR"
  | "ENCARGADO_LOGISTICA"
  | "COUNTER"
  | "MOTORIZADO";

export interface UsuarioAdministracion {
  id: number;
  nombre: string;
  correo: string;
  agenciaId: number | null;
  sede: string;
  rol: RolInternoUsuario;
  activo: boolean;
}

export interface CrearUsuarioAdministracion {
  nombre: string;
  correo: string;
  clave: string;
  rol: RolInternoUsuario;
  agenciaId: number | null;
}

export interface ActualizarRolAdministracion {
  rol: RolInternoUsuario;
  agenciaId: number | null;
}