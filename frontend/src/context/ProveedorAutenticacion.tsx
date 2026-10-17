import { useState, type ReactNode } from "react";
import { ContextoAutenticacion, type Usuario } from "./ContextoAutenticacion";

export function ProveedorAutenticacion({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);

  function iniciarSesion(nuevoUsuario: Usuario) {
    setUsuario(nuevoUsuario);
  }

  function cerrarSesion() {
    setUsuario(null);
  }

  return (
    <ContextoAutenticacion.Provider
      value={{
        usuario,
        iniciarSesion,
        cerrarSesion,
      }}
    >
      {children}
    </ContextoAutenticacion.Provider>
  );
}

