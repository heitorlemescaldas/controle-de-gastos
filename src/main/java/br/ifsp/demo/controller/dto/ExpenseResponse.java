package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;

import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseResponse(
        String id,
        String userId,
        BigDecimal amount,
        ExpenseType type,
        String description,
        Instant timestamp,
        String categoryId
) {
    public static ExpenseResponse fromDomain(Expense e) {
        return new ExpenseResponse(
                e.id(),
                e.userId(),
                e.amount(),
                e.type(),
                e.description(),
                e.occurredAt(),
                e.categoryId()
        );
    }
}