/**
 * Armazena o JWT no localStorage e expõe helpers simples.
 * Também oferece um mini pub/sub para reagir a mudanças de sessão.
 */

const TOKEN_KEY = "auth_token";

export type TokenListener = (token: string | null) => void;

const listeners = new Set<TokenListener>();

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token);
    notify();
  } catch {
    // ignore
  }
}

export function clearToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY);
    notify();
  } catch {
    // ignore
  }
}

/** Assina mudanças do token (login/logout). */
export function subscribe(listener: TokenListener): () => void {
  listeners.add(listener);
  // dispara imediatamente com o estado atual
  try {
    listener(getToken());
  } catch {}
  return () => listeners.delete(listener);
}

function notify() {
  const current = getToken();
  for (const l of listeners) {
    try {
      l(current);
    } catch {
      // ignore listener errors
    }
  }
}