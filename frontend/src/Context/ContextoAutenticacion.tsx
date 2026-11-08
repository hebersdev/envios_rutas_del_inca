import { createContext } from "react";

export interface Usuario {
  nombre: string;
  correo: string;
  rol: "CLIENTE" | "COUNTER" | "MOTORIZADO" | "ENCARGADO_LOGISTICA" | "ADMINISTRADOR";
  agenciaId?: number | null;
}

export interface ContextoAutenticacionType {
  usuario: Usuario | null;
  cargando: boolean;
  errorCarga: string | null;
  iniciarSesion: (usuario: Usuario) => void;
  cerrarSesion: () => void;
  reintentarCarga: () => Promise<void>;
}

export const ContextoAutenticacion = createContext<ContextoAutenticacionType | undefined>(undefined);