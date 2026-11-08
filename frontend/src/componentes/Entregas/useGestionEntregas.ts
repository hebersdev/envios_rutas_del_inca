import { useEffect, useRef, useState } from "react";
import { listarMisEntregas, listarMotorizados, listarPorEntregar, listarReparto } from "../../Servicios/Entregas";
import type { Entrega, MotorizadoReparto, VistaEntregas } from "../../Tipos/Entrega";

interface Resultado {
  revision: number;
  envios: Entrega[];
  motorizados: MotorizadoReparto[];
  error: string;
}

export function useGestionEntregas(vista: VistaEntregas) {
  const [revision, setRevision] = useState(0);
  const [resultado, setResultado] = useState<Resultado | null>(null);
  const [seleccion, setSeleccion] = useState<number[]>([]);
  const [ocupado, setOcupado] = useState(false);
  const guardando = useRef(false);
  const [mensaje, setMensaje] = useState("");
  const [errorAccion, setErrorAccion] = useState("");
  const cargando = resultado?.revision !== revision;

  useEffect(() => {
    let vigente = true;
    const consulta = vista === "COUNTER" ? listarPorEntregar() : vista === "REPARTO" ? listarReparto() : listarMisEntregas();
    Promise.all([consulta, vista === "REPARTO" ? listarMotorizados() : Promise.resolve([])])
      .then(([envios, motorizados]) => {
        if (vigente) setResultado({ revision, envios, motorizados, error: "" });
      }).catch((error: unknown) => {
        if (vigente) setResultado({ revision, envios: [], motorizados: [],
          error: error instanceof Error ? error.message : "No se pudo cargar la información." });
      });
    return () => { vigente = false; };
  }, [vista, revision]);

  function actualizar() {
    setSeleccion([]);
    setRevision(valor => valor + 1);
  }

  function seleccionar(id: number) {
    setSeleccion(actual => actual.includes(id) ? actual.filter(valor => valor !== id) : [...actual, id]);
  }

  function entregaCompletada() {
    setMensaje("La entrega se confirmó correctamente.");
    setErrorAccion("");
    actualizar();
  }

  async function ejecutar(operacion: () => Promise<void>, exito: string) {
    if (guardando.current) return;
    guardando.current = true;
    setOcupado(true);
    setMensaje("");
    setErrorAccion("");
    try {
      await operacion();
      setMensaje(exito);
      actualizar();
    } catch (error: unknown) {
      setErrorAccion(error instanceof Error ? error.message : "No se pudo completar la operación.");
    } finally {
      guardando.current = false;
      setOcupado(false);
    }
  }

  return { envios: resultado?.envios ?? [], motorizados: resultado?.motorizados ?? [],
    error: resultado?.error ?? "", cargando, ocupado, mensaje, errorAccion,
    seleccion, setSeleccion, seleccionar, actualizar, entregaCompletada, ejecutar };
}
