import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { evaluateMonthlyGoal, setMonthlyGoal, type GoalEvaluation, type SetGoalRequest } from "@/services/goals";

export function useGoalEvaluation(params: { rootCategoryId: string; month: string }) {
  return useQuery<GoalEvaluation>({
    queryKey: ["goal:evaluate", params],
    queryFn: () => evaluateMonthlyGoal(params),
    enabled: !!params.rootCategoryId && !!params.month,
  });
}

export function useSetGoal() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: SetGoalRequest) => setMonthlyGoal(body),
    onSuccess: (_data, variables) => {
      qc.invalidateQueries({ queryKey: ["goal:evaluate", { rootCategoryId: variables.rootCategoryId, month: variables.month }] });
    },
  });
}
