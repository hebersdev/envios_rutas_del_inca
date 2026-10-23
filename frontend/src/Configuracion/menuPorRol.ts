import type { Usuario } from "../Context/ContextoAutenticacion";

export const opcionesMenu: {
  etiqueta: string;
  ruta: string;
  roles: Usuario["rol"][];
}[] = [
  { etiqueta: "Inicio", ruta: "/panel", roles: ["ADMINISTRADOR", "COUNTER", "MOTORIZADO", "ENCARGADO_LOGISTICA"] },
  { etiqueta: "Sedes", ruta: "/panel/agencias", roles: ["ADMINISTRADOR"] },
  { etiqueta: "Usuarios", ruta: "/panel/usuarios", roles: ["ADMINISTRADOR"] },
  { etiqueta: "Camiones", ruta: "/panel/Camiones", roles: ["ADMINISTRADOR", "ENCARGADO_LOGISTICA"] },
  { etiqueta: "Tarifas", ruta: "/panel/tarifas", roles: ["ADMINISTRADOR"] },
];
