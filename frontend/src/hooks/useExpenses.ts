import { api } from "@/lib/api";

/** Schemas conforme Swagger */
export type CreateExpenseRequest = {
  amount: string;
  type: "DEBIT" | "CREDIT";
  description: string;
  timestamp: string;   // ISO ex: 2025-11-03T21:30:00Z
  categoryId: string;
};

/** POST /expenses */
export async function createExpense(body: CreateExpenseRequest): Promise<void> {
  await api.post<void>("/expenses", body);
}

/** Helpers */
export function toIsoTimestampFromDate(dateStr: string): string {
  // dateStr = "YYYY-MM-DD". Gera meio-dia UTC para evitar problemas de timezone.
  // (Se quiser a hora atual do usuário, pode trocar para new Date().toISOString())
  const [y, m, d] = dateStr.split("-").map(Number);
  const dt = new Date(Date.UTC(y, m - 1, d, 12, 0, 0, 0));
  return dt.toISOString(); // sempre termina com 'Z'
}