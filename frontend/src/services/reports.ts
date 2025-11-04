import { api } from "@/lib/api";

/** Tipos conforme Swagger */
export type ReportItem = {
  categoryPath: string;
  debit: number;
  credit: number;
};

export type Report = {
  userId: string;
  start: string;       // $date-time
  end: string;         // $date-time
  totalDebit: number;
  totalCredit: number;
  balance: number;
  items: ReportItem[];
};

/** Helper para montar query string */
function qs(params: Record<string, string | number | undefined>) {
  const s = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join("&");
  return s ? `?${s}` : "";
}

/**
 * GET /api/v1/reports/period?start=YYYY-MM-DD&end=YYYY-MM-DD
 * Observação: o Swagger mostra start/end como $date-time; se você só tiver a data,
 * pode enviar "YYYY-MM-DD" ou completar com "T00:00:00".
 */
export async function getPeriodReport(params: { start: string; end: string }): Promise<Report> {
  const url = `/api/v1/reports/period${qs(params)}`;
  return api.get<Report>(url);
}

/**
 * GET /api/v1/reports/category-tree?start=...&end=...&rootCategoryId=...
 * (conforme Swagger)
 */
export async function getCategoryTreeReport(params: {
  start: string;
  end: string;
  rootCategoryId: string;
}): Promise<Report> {
  const url = `/api/v1/reports/category-tree${qs(params)}`;
  return api.get<Report>(url);
}
