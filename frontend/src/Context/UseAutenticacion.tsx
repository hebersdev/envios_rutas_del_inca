import { useContext } from "react";
import { ContextoAutenticacion } from "./ContextoAutenticacion.tsx";

export function useAutenticacion() {
  const contexto = useContext(ContextoAutenticacion);

  if (!contexto) {
    throw new Error("useAuth debe usarse dentro de un AuthProvider");
  }

  return contexto;
}