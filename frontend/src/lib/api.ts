import { getToken, clearToken } from "@/stores/session";

const BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, "") ?? "";

if (!BASE_URL) {
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
  auth?: boolean; // default: true
  body?: unknown;
  headers?: Record<string, string>;
};

function extractSubFromJwt(token: string | null): string | null {
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    const json = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return json?.sub ?? null; // nosso backend usa "sub" = email
  } catch {
    return null;
  }
}

function buildHeaders(auth: boolean, extra?: Record<string, string>) {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...extra,
  };

  if (auth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;

    const sub = extractSubFromJwt(token);
    if (sub) headers["X-User"] = sub; // obrigatório para os endpoints protegidos
  }
  return headers;
}

async function handleResponse<T>(res: Response): Promise<T> {
  const contentType = res.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await res.json().catch(() => null) : await res.text().catch(() => null);

  if (res.ok) {
    // retorna body se JSON; se não, undefined
    return (isJson ? (body as T) : (undefined as unknown as T));
  }

  if (res.status === 401) {
    clearToken();
    try {
      if (typeof window !== "undefined" && window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    } catch {
      // ignore
    }
  }

  const message =
    (isJson && (body as any)?.message) ||
    (isJson && (body as any)?.error) ||
    `HTTP ${res.status}`;
  throw new ApiError(message, res.status, body);
}

async function request<T>(method: HttpMethod, path: string, options: RequestOptions = {}) {
  const { auth = true, body, headers: extraHeaders, ...rest } = options;

  // path SEM /api/v1 — já está no BASE_URL
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const url = `${BASE_URL}${normalizedPath}`;

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

// Atalhos comuns da nossa API (paths SEM /api/v1)
export const authApi = {
  register: (payload: { email: string; password: string }) =>
    api.post<void>("/register", payload, { auth: false }),
  login: (payload: { email: string; password: string }) =>
    api.post<{ token: string }>("/authenticate", payload, { auth: false }),
};

export const categoryApi = {
  list: () => api.get<any[]>("/categories"),
  createRoot: (name: string) => api.post<any>("/categories", { name }),
  createChild: (parentId: string, name: string) =>
    api.post<any>(`/categories/${parentId}/children`, { name }),
  rename: (id: string, newName: string) =>
    api.patch<void>(`/categories/${id}/rename`, { newName }),
  move: (id: string, newParentId: string) =>
    api.patch<void>(`/categories/${id}/move`, { newParentId }),
  remove: (id: string) => api.del<void>(`/categories/${id}`),
};

export const expenseApi = {
  create: (payload: {
    amount: number | string;
    type: "DEBIT" | "CREDIT";
    description: string;
    timestamp: string; // use new Date().toISOString()
    categoryId?: string;
  }) => api.post<any>("/expenses", payload),
};

export const reportApi = {
  byPeriod: (startIso: string, endIso: string) =>
    api.get<any>(`/reports/period?start=${encodeURIComponent(startIso)}&end=${encodeURIComponent(endIso)}`),
  byCategoryTree: (startIso: string, endIso: string, rootCategoryId: string) =>
    api.get<any>(`/reports/category-tree?start=${encodeURIComponent(startIso)}&end=${encodeURIComponent(endIso)}&rootCategoryId=${encodeURIComponent(rootCategoryId)}`),
};

export const goalApi = {
  setMonthly: (payload: { rootCategoryId: string; month: string; limit: number | string }) =>
    api.post<any>("/goals", payload),
  evaluate: (rootCategoryId: string, month: string) =>
    api.get<any>(`/goals/evaluate?rootCategoryId=${encodeURIComponent(rootCategoryId)}&month=${encodeURIComponent(month)}`),
};

// teste simples
export async function pingHello(): Promise<string> {
  // path sem /api/v1
  const res = await api.get<string>("/hello");
  return res;
}