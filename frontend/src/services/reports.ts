import { api } from "@/lib/api";

export type ReportItem = { categoryPath: string; debit: number; credit: number };
export type Report = {
  userId: string;
  start: string;
  end: string;
  totalDebit: number;
  totalCredit: number;
  balance: number;
  items: ReportItem[];
};

function qs(params: Record<string, string | number | undefined>) {
  const s = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join("&");
  return s ? `?${s}` : "";
}

/** Gera início/fim de mês em ISO UTC completo (Instant.parse no back aceita) */
export function monthStartEndISO(yyyyMM: string) {
  const [y, m] = yyyyMM.split("-").map(Number);
  const start = new Date(Date.UTC(y, m - 1, 1, 0, 0, 0, 0)); // 1º dia 00:00:00Z
  const nextMonth = new Date(Date.UTC(y, m, 1, 0, 0, 0, 0));
  const end = new Date(nextMonth.getTime() - 1); // último ms do mês
  return { start: start.toISOString(), end: end.toISOString() };
}

export async function getPeriodReport(params: { start: string; end: string }): Promise<Report> {
  const url = `/reports/period${qs(params)}`;
  return api.get<Report>(url);
}

export async function getCategoryTreeReport(params: {
  start: string;
  end: string;
  rootCategoryId: string;
}): Promise<Report> {
  const url = `/reports/category-tree${qs(params)}`;
  return api.get<Report>(url);
}