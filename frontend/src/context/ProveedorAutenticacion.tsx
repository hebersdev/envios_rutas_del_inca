import { useState, type ReactNode } from "react";
import { ContextoAutenticacion, type Usuario } from "./ContextoAutenticacion";

export function ProveedorAutenticacion({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [usuario, setUsuario] = useState<Usuario | null>(null);

  function iniciarSesion(nuevoToken: string, nuevoUsuario: Usuario) {
    setToken(nuevoToken);
    setUsuario(nuevoUsuario);
  }

  function cerrarSesion() {
    setToken(null);
    setUsuario(null);
  }

  return (
    <ContextoAutenticacion.Provider
      value={{
        usuario,
        token,
        iniciarSesion,
        cerrarSesion,
      }}
    >
      {children}
    </ContextoAutenticacion.Provider>
  );
}

