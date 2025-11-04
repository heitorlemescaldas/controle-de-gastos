import { useQuery } from "@tanstack/react-query";
import { getPeriodReport, getCategoryTreeReport, type Report } from "@/services/reports";

export function usePeriodReport(params: { start: string; end: string }) {
  return useQuery<Report>({
    queryKey: ["report:period", params],
    queryFn: () => getPeriodReport(params),
  });
}

export function useCategoryTreeReport(params: { start: string; end: string; rootCategoryId: string }) {
  return useQuery<Report>({
    queryKey: ["report:categoryTree", params],
    queryFn: () => getCategoryTreeReport(params),
    enabled: !!params.rootCategoryId, // só busca quando tiver categoria
  });
}
