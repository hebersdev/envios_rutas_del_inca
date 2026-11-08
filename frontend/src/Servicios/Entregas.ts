import { clienteApi } from "./ClienteApi";
import type { Entrega, MotorizadoReparto } from "../Tipos/Entrega";

const ruta = "/api/entregas";
export const listarPorEntregar = () => clienteApi<Entrega[]>(`${ruta}/por-entregar`);
export const listarReparto = () => clienteApi<Entrega[]>(`${ruta}/reparto`);
export const listarMisEntregas = () => clienteApi<Entrega[]>(`${ruta}/mis-entregas`);
export const listarMotorizados = () => clienteApi<MotorizadoReparto[]>(`${ruta}/motorizados`);
export const marcarListos = (envioIds: number[]) =>
  clienteApi<void>(`${ruta}/listo-recojo`, { method: "POST", body: { envioIds } });
export const asignarMotorizado = (envioIds: number[], motorizadoId: number) =>
  clienteApi<void>(`${ruta}/asignar-motorizado`, { method: "POST", body: { envioIds, motorizadoId } });
export const salirReparto = (envioIds: number[]) =>
  clienteApi<void>(`${ruta}/salir-a-reparto`, { method: "POST", body: { envioIds } });
export const confirmarEntrega = (id: number, documento: string, enAgencia: boolean) =>
  clienteApi<void>(`${ruta}/${id}/${enAgencia ? "entregar-en-agencia" : "entregar"}`,
    { method: "POST", body: { documento } });
