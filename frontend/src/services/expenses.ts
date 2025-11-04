import { api } from "@/lib/api";

/** Schemas conforme Swagger */
export type CreateExpenseRequest = {
  amount: string;        // usar string para evitar problemas de ponto/locale
  type: "DEBIT" | "CREDIT";
  description: string;
  timestamp: string;     // ISO ex: 2025-11-03T21:30:00
  categoryId: string;    // UUID
};

/** POST /api/v1/expenses */
export async function createExpense(body: CreateExpenseRequest): Promise<void> {
  await api.post<void>("/api/v1/expenses", body);
}

/** Helper opcional: cria ISO a partir de Date ou YYYY-MM-DD */
export function toIsoTimestamp(input: Date | string): string {
  if (input instanceof Date) return input.toISOString().slice(0, 19);
  // se vier "YYYY-MM-DD", completa com HH:mm:ss = 00:00:00
  if (/^\d{4}-\d{2}-\d{2}$/.test(input)) return `${input}T00:00:00`;
  return input; // já é ISO
}