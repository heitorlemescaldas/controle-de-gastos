import { api } from "@/lib/api";
import { setToken } from "@/stores/session";

/** Schemas do Swagger */
export type AuthRequest = {
  username: string;
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
 * POST /api/v1/authenticate
 * Body: { username, password }
 * Res:  { token }
 */
export async function login(payload: AuthRequest): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>("/api/v1/authenticate", payload, {
    auth: false, // login não envia Authorization
  });
  if (res?.token) {
    setToken(res.token);
  }
  return res;
}

/**
 * POST /api/v1/register
 * Body: { name, lastname, email, password }
 * Res: vazio ou algum DTO (back pode retornar 200/201 sem body)
 */
export async function register(payload: RegisterUserRequest): Promise<void> {
  await api.post<void>("/api/v1/register", payload, { auth: false });
}