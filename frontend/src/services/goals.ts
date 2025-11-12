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
  month: string;            // simplificado (YYYY-MM)
  limit: number;
  spent: number;
  exceeded: boolean;
  diff: number;
};

/** POST /goals */
export async function setMonthlyGoal(body: SetGoalRequest): Promise<void> {
  await api.post<void>("/goals", body);
}

/**
 * GET /goals/evaluate?rootCategoryId=...&month=YYYY-MM
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
  month: string;
}): Promise<GoalEvaluation> {
  const url = `/goals/evaluate${qs(params)}`;
  return api.get<GoalEvaluation>(url);
}