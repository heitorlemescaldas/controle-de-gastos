/**
 * Cliente HTTP minimalista usando fetch.
 * - Base URL: import.meta.env.VITE_API_BASE_URL
 * - Injeta Authorization: Bearer <token> (se existir)
 * - Trata 401: limpa sessão e redireciona para /login
 * - Normaliza erros em uma ApiError
 */

import { getToken, clearToken } from "@/stores/session";

const BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, "") ?? "";

if (!BASE_URL) {
  // Ajuda durante dev caso esqueça o .env
  // eslint-disable-next-line no-console
  console.warn("VITE_API_BASE_URL não configurada. Defina no arquivo .env");
}

export class ApiError extends Error {
  status: number;
  body: unknown;
  constructor(message: string, status: number, body: unknown) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type RequestOptions = Omit<RequestInit, "method" | "body" | "headers"> & {
  /** Quando true (padrão), envia Authorization se houver token. */
  auth?: boolean;
  /** Corpo JSON serializável. */
  body?: unknown;
  /** Headers adicionais. */
  headers?: Record<string, string>;
};

/** Monta headers padrão + Authorization (se houver) */
function buildHeaders(auth: boolean, extra?: Record<string, string>) {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...extra,
  };

  if (auth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

async function handleResponse<T>(res: Response): Promise<T> {
  // Tenta ler JSON quando houver.
  const contentType = res.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await res.json().catch(() => null) : await res.text().catch(() => null);

  if (res.ok) {
    return (isJson ? (body as T) : (undefined as unknown as T));
  }

  if (res.status === 401) {
    // Sessão inválida/expirada → limpa e manda pro login
    clearToken();
    try {
      if (typeof window !== "undefined" && window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    } catch {
      // ignore
    }
  }

  // Levanta um erro rico para os callers tratarem (toasts, etc.)
  const message =
    (isJson && (body as any)?.message) ||
    (isJson && (body as any)?.error) ||
    `HTTP ${res.status}`;
  throw new ApiError(message, res.status, body);
}

async function request<T>(method: HttpMethod, path: string, options: RequestOptions = {}): Promise<T> {
  const { auth = true, body, headers: extraHeaders, ...rest } = options;

  const url = `${BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;

  const init: RequestInit = {
    method,
    headers: buildHeaders(auth, extraHeaders),
    ...rest,
  };

  if (body !== undefined) {
    init.body = typeof body === "string" ? body : JSON.stringify(body);
  }

  const res = await fetch(url, init);
  return handleResponse<T>(res);
}

// Helpers verbais
export const api = {
  get:   <T>(path: string, options?: RequestOptions) => request<T>("GET", path, options),
  post:  <T>(path: string, body?: unknown, options?: RequestOptions) =>
           request<T>("POST", path, { ...options, body }),
  put:   <T>(path: string, body?: unknown, options?: RequestOptions) =>
           request<T>("PUT", path, { ...options, body }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
           request<T>("PATCH", path, { ...options, body }),
  del:   <T>(path: string, options?: RequestOptions) => request<T>("DELETE", path, options),
};

// Pequenos atalhos úteis durante dev
export async function pingHello(): Promise<string> {
  // endpoint público ou protegido? Se protegido, remover auth:false
  const res = await api.get<string>("/api/v1/hello", { auth: true });
  return res;
}