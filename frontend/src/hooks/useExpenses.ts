import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createExpense, type CreateExpenseRequest } from "@/services/expenses";

export function useExpenses() {
  const qc = useQueryClient();

  const create = useMutation({
    mutationFn: (body: CreateExpenseRequest) => createExpense(body),
    onSuccess: () => {
      // Atualiza relatórios e qualquer lista que dependa de despesas
      qc.invalidateQueries({ queryKey: ["report:period"] });
      qc.invalidateQueries({ queryKey: ["report:categoryTree"] });
    },
  });

  return { create };
}
