interface RespuestaError {
  mensaje?: string;
  errores?: string | string[] | Record<string, string>;
}

interface RespuestaCsrf {
  token: string;
  headerName: string;
}

interface OpcionesClienteApi {
  method?: string;
  body?: unknown;
  headers?: HeadersInit;
}

const apiUrl = (import.meta.env.VITE_API_URL ?? "").replace(/\/$/, "");

async function obtenerTokenCsrf(): Promise<RespuestaCsrf> {
  const respuesta = await fetch(`${apiUrl}/api/csrf`, {
    credentials: "include",
  });

  if (!respuesta.ok) {
    throw new Error("No fue posible preparar una solicitud segura.");
  }

  return await respuesta.json() as RespuestaCsrf;
}

function mensajeDeError(contenido: RespuestaError | null, status: number): string {
  if (status === 403) {
    return "No tienes permiso para esta acción";
  }
  if (contenido?.mensaje) {
    return contenido.mensaje;
  }
  if (typeof contenido?.errores === "string") {
    return contenido.errores;
  }
  if (Array.isArray(contenido?.errores)) {
    return contenido.errores.join(", ");
  }
  if (contenido?.errores) {
    return Object.values(contenido.errores).join(", ");
  }
  return "No se pudo completar la solicitud.";
}

export async function clienteApi<T>(
  ruta: string,
  opciones: OpcionesClienteApi = {},
): Promise<T> {
  const method = (opciones.method ?? "GET").toUpperCase();
  const headers = new Headers(opciones.headers);
  let body: string | undefined;

  if (opciones.body !== undefined) {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(opciones.body);
  }

  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    const csrf = await obtenerTokenCsrf();
    headers.set(csrf.headerName, csrf.token);
  }

  let respuesta: Response;
  try {
    respuesta = await fetch(`${apiUrl}${ruta}`, {
      method,
      headers,
      body,
      credentials: "include",
    });
  } catch {
    throw new Error("No fue posible conectar con el servidor. Inténtalo nuevamente.");
  }

  if (respuesta.status === 204) {
    return undefined as T;
  }

  const contenido = await respuesta.json().catch(() => null) as RespuestaError | null;

  if (respuesta.status === 401) {
    const esCotizacionPublica = ruta.startsWith("/api/cotizaciones");
    if (!esCotizacionPublica && window.location.pathname !== "/auth") {
      window.location.assign("/auth");
    }
    throw new Error(mensajeDeError(contenido, respuesta.status));
  }

  if (!respuesta.ok) {
    throw new Error(mensajeDeError(contenido, respuesta.status));
  }

  return contenido as T;
}
