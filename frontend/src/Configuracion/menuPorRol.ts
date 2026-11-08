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
  {etiqueta: "Envios", ruta: "/panel/envios", roles: ['COUNTER']},
  { etiqueta: "Viajes", ruta: "/panel/viajes", roles: ["ENCARGADO_LOGISTICA"] },
  { etiqueta: "Reportes", ruta: "/panel/reportes", roles: ["ENCARGADO_LOGISTICA"] },
  { etiqueta: "Pendientes sin camión", ruta: "/panel/pendientes", roles: ["ENCARGADO_LOGISTICA"] },
  { etiqueta: "Llegadas", ruta: "/panel/llegadas", roles: ["ENCARGADO_LOGISTICA"] },
  { etiqueta: "Por entregar", ruta: "/panel/por-entregar", roles: ["COUNTER"] },
  { etiqueta: "Reparto", ruta: "/panel/reparto", roles: ["ENCARGADO_LOGISTICA"] },
  { etiqueta: "Mis entregas", ruta: "/panel/mis-entregas", roles: ["MOTORIZADO"] }
];
