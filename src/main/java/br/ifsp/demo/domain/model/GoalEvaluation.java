package br.ifsp.demo.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record GoalEvaluation(
        String userId,
        String categoryId,
        YearMonth month,
        BigDecimal limit,
        BigDecimal spent,
        boolean exceeded,
        BigDecimal diff // spent - limit (>= 0 quando exceeded, senão 0.00)
) { }