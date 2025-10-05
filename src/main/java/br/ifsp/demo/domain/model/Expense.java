package br.ifsp.demo.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public final class Expense {
    private final String id;
    private final String userId;
    private final BigDecimal amount;
    private final ExpenseType type;
    private final String description;
    private final Instant occurredAt;
    private final String categoryId;

    private Expense(String id, String userId, BigDecimal amount, ExpenseType type,
                    String description, Instant occurredAt, String categoryId) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.occurredAt = occurredAt;
        this.categoryId = categoryId;
    }

    public static Expense of(String userId, BigDecimal amount, ExpenseType type,
                             String description, Instant occurredAt, String categoryId) {
        return new Expense(null, userId, amount, type, description, occurredAt, categoryId);
    }

    public Expense withId(String id) {
        return new Expense(id, userId, amount, type, description, occurredAt, categoryId);
    }

    public String id() { return id; }
    public String userId() { return userId; }
    public BigDecimal amount() { return amount; }
    public ExpenseType type() { return type; }
    public String description() { return description; }
    public Instant occurredAt() { return occurredAt; }
    public String categoryId() { return categoryId; }

    public Expense withDescription(String newDescription) {
        return new Expense(id, userId, amount, type, newDescription, occurredAt, categoryId);
    }
}
