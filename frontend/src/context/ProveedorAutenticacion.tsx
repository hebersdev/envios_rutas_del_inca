import { useCallback, useEffect, useState, type ReactNode } from "react";
import { ContextoAutenticacion, type Usuario } from "./ContextoAutenticacion";
import { obtenerSesion } from "../Servicios/Autenticacion";

export function ProveedorAutenticacion({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState<string | null>(null);

  const cargarSesion = useCallback(async () => {
    try {
      setUsuario(await obtenerSesion());
    } catch (error) {
      setErrorCarga(error instanceof Error
        ? error.message
        : "No se pudo verificar la sesión.");
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    let componenteMontado = true;

    obtenerSesion()
      .then((sesion) => {
        if (componenteMontado) setUsuario(sesion);
      })
      .catch((error: unknown) => {
        if (componenteMontado) {
          setErrorCarga(error instanceof Error
            ? error.message
            : "No se pudo verificar la sesión.");
        }
      })
      .finally(() => {
        if (componenteMontado) setCargando(false);
      });

    return () => {
      componenteMontado = false;
    };
  }, []);

  async function reintentarCarga() {
    setCargando(true);
    setErrorCarga(null);
    await cargarSesion();
  }

  function iniciarSesion(nuevoUsuario: Usuario) {
    setUsuario(nuevoUsuario);
    setErrorCarga(null);
  }

  function cerrarSesion() {
    setUsuario(null);
  }

  return (
    <ContextoAutenticacion.Provider
      value={{
        usuario,
        cargando,
        errorCarga,
        iniciarSesion,
        cerrarSesion,
        reintentarCarga,
      }}
    >
      {children}
    </ContextoAutenticacion.Provider>
  );
}
