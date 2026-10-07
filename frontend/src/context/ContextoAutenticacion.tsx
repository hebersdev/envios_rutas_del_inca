import { createContext } from "react";

export interface Usuario {
  nombre: string;
  rol: "CLIENTE" | "COUNTER" | "MOTORIZADO" | "ENCARGADO_LOGISTICA" | "ADMINISTRADOR";
}

export interface ContextoAutenticacionType {
  usuario: Usuario | null;
  token: string | null;
  iniciarSesion: (token: string, usuario: Usuario) => void;
  cerrarSesion: () => void;
}

export const ContextoAutenticacion = createContext<ContextoAutenticacionType | undefined>(undefined);