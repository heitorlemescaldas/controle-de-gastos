package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.Goal;

import java.math.BigDecimal;
import java.time.YearMonth;

public record GoalResponse(
        String id,
        String userId,
        String categoryId,
        YearMonth month,
        BigDecimal limit
) {
    public static GoalResponse fromDomain(Goal g) {
        return new GoalResponse(
                g.id(),
                g.userId(),
                g.categoryId(),
                g.month(),
                g.limitAmount()
        );
    }
}