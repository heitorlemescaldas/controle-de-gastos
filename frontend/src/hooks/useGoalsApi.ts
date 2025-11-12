import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";

type SetGoalReq = { rootCategoryId: string; month: string; limit: string | number };
type GoalEval = {
  userId: string;
  categoryId: string;
  month: string;   // yyyy-MM no back
  limit: number;
  spent: number;
  exceeded: boolean;
  diff: number;
};

// helpers
function qs(params: Record<string, string>) {
  const s = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join("&");
  return s ? `?${s}` : "";
}

// GET /goals/evaluate
export function useGoalEvaluation(opts: { rootCategoryId?: string; month?: string }) {
  const { rootCategoryId, month } = opts;
  return useQuery<GoalEval>({
    queryKey: ["goal-eval", rootCategoryId ?? "", month ?? ""],
    enabled: !!rootCategoryId && !!month,
    queryFn: async () => {
      const url = `/goals/evaluate${qs({ rootCategoryId: rootCategoryId!, month: month! })}`;
      return api.get<GoalEval>(url);
    },
  });
}

// POST /goals
export function useSetGoal() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: SetGoalReq) => {
      // garantir string com ponto
      const payload = {
        ...body,
        limit: String(body.limit).replace(",", "."),
      };
      return api.post<void>("/goals", payload);
    },
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["goal-eval", vars.rootCategoryId, vars.month] });
    },
  });
}