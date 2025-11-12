import { api } from "@/lib/api";
import { setToken } from "@/stores/session";

/** Schemas do Swagger */
export type AuthRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  token: string;
};

export type RegisterUserRequest = {
  name: string;
  lastname: string;
  email: string;
  password: string;
};

/**
 * POST /authenticate
 * Body: { email, password }
 * Res:  { token }
 */
export async function login(payload: AuthRequest): Promise<AuthResponse> {
  // envia ambos: email e username (por compatibilidade com o backend)
  const body = { email: payload.email, username: payload.email, password: payload.password };

  const res = await api.post<AuthResponse>("/authenticate", body, { auth: false });
  if (res?.token) {
    setToken(res.token);
    try { localStorage.setItem("email", payload.email); } catch {}
  }
  return res;
}

/**
 * POST /register
 * Body: { name, lastname, email, password }
 * Res: vazio ou {id} dependendo do back
 */
export async function register(payload: RegisterUserRequest): Promise<void> {
  await api.post<void>("/register", payload, { auth: false });
}