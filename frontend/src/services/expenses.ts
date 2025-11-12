import { api } from "@/lib/api";

/** Schemas conforme Swagger */
export type CreateExpenseRequest = {
  amount: string;              // string p/ evitar locale
  type: "DEBIT" | "CREDIT";
  description: string;
  timestamp: string;           // ISO completo: 2025-11-11T23:45:10.123Z
  categoryId: string;          // UUID
};

/** POST /expenses */
export async function createExpense(body: CreateExpenseRequest): Promise<void> {
  await api.post<void>("/expenses", body);
}

/** Helper: sempre retorna ISO com 'Z' */
export function toIsoTimestamp(input: Date | string): string {
  if (input instanceof Date) {
    // ISO completo com milissegundos e Z
    return input.toISOString();
  }
  // Se vier "YYYY-MM-DD", completa com meia-noite UTC
  if (/^\d{4}-\d{2}-\d{2}$/.test(input)) {
    return `${input}T00:00:00.000Z`;
  }
  return input; // já é ISO
}