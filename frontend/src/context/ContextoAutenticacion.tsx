import { createContext } from "react";

export interface Usuario {
  nombre: string;
  correo: string;
  rol: "cliente" | "counter" | "motorizado" | "encargado_logistica" | "administrador";
}

export interface ContextoAutenticacionType {
  usuario: Usuario | null;
  iniciarSesion: (usuario: Usuario) => void;
  cerrarSesion: () => void;
}

export const ContextoAutenticacion = createContext<ContextoAutenticacionType | undefined>(undefined);