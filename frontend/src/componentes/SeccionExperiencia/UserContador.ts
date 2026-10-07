import { useEffect, useRef, useState } from "react";

interface OpcionesContador {
    meta: number;
    duracion?: number;
}

function useContador({ meta, duracion = 2000 }: OpcionesContador) {
    const referencia = useRef<HTMLSpanElement>(null);
    const [valor, setValor] = useState(0);

    useEffect(() => {
        const elemento = referencia.current;

        if (!elemento) return;

        let idAnimacion = 0;

        const animar = () => {
            const prefiereSinMovimiento = window.matchMedia(
                "(prefers-reduced-motion: reduce)"
            ).matches;

            if (prefiereSinMovimiento) {
                setValor(meta);
                return;
            }

            const inicio = performance.now();

            const paso = (ahora: number) => {
                const progreso = Math.min((ahora - inicio) / duracion, 1);
                const suavizado = 1 - Math.pow(1 - progreso, 3);

                setValor(Math.round(meta * suavizado));

                if (progreso < 1) {
                    idAnimacion = requestAnimationFrame(paso);
                }
            };

            idAnimacion = requestAnimationFrame(paso);
        };

        const observador = new IntersectionObserver(
            ([entrada]) => {
                if (entrada.isIntersecting) {
                    animar();
                    observador.disconnect();
                }
            },
            { threshold: 1.0 }
        );

        observador.observe(elemento);

        return () => {
            observador.disconnect();
            cancelAnimationFrame(idAnimacion);
        };
    }, [meta, duracion]);

    return { referencia, valor };
}

export default useContador;