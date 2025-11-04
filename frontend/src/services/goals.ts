import { api } from "@/lib/api";

/** Tipos conforme Swagger */
export type SetGoalRequest = {
  rootCategoryId: string;   // UUID
  month: string;            // "YYYY-MM"
  limit: string;            // usar string para valores monetários (ex.: "500.00")
};

export type GoalEvaluation = {
  userId: string;
  categoryId: string;
  month: {
    year: number;
    month: string;        // "JANUARY" ... "DECEMBER"
    monthValue: number;   // 1..12
    leapYear: boolean;
  };
  limit: number;
  spent: number;
  exceeded: boolean;
  diff: number;           // positivo se há saldo, negativo se excedeu
};

/** POST /api/v1/goals */
export async function setMonthlyGoal(body: SetGoalRequest): Promise<void> {
  await api.post<void>("/api/v1/goals", body);
}

/**
 * GET /api/v1/goals/evaluate?rootCategoryId=...&month=YYYY-MM
 * (conforme Swagger)
 */
function qs(params: Record<string, string>) {
  const s = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join("&");
  return s ? `?${s}` : "";
}

export async function evaluateMonthlyGoal(params: {
  rootCategoryId: string;
  month: string;     // "YYYY-MM"
}): Promise<GoalEvaluation> {
  const url = `/api/v1/goals/evaluate${qs(params)}`;
  return api.get<GoalEvaluation>(url);
}
