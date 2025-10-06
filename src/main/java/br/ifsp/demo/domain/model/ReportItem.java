package br.ifsp.demo.domain.model;

import java.math.BigDecimal;

public record ReportItem(
        String categoryPath, // ex.: "Alimentação/Mercado" ou "Sem categoria"
        BigDecimal debit,
        BigDecimal credit
) implements Comparable<ReportItem> {
    @Override public int compareTo(ReportItem o) {
        return this.categoryPath.compareToIgnoreCase(o.categoryPath);
    }

    public ReportItem addDebit(java.math.BigDecimal v) {
        return new ReportItem(categoryPath, debit.add(v), credit);
    }
    public ReportItem addCredit(java.math.BigDecimal v) {
        return new ReportItem(categoryPath, debit, credit.add(v));
    }
}