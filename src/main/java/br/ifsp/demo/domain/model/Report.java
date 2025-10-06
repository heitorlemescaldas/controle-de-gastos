package br.ifsp.demo.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Report(
        String userId,
        Instant start,
        Instant end,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal balance,
        List<ReportItem> items
) {}