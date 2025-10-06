package br.ifsp.demo.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public final class Goal {
    private final String id;
    private final String userId;
    private final String categoryId; // deve ser RAIZ para este cenário
    private final YearMonth month;   // meta mensal
    private final BigDecimal limitAmount;

    private Goal(String id, String userId, String categoryId, YearMonth month, BigDecimal limitAmount) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    public static Goal monthly(String userId, String categoryId, YearMonth month, BigDecimal limitAmount) {
        return new Goal(null, userId, categoryId, month, limitAmount);
    }

    public Goal withId(String id) { return new Goal(id, userId, categoryId, month, limitAmount); }

    public String id() { return id; }
    public String userId() { return userId; }
    public String categoryId() { return categoryId; }
    public YearMonth month() { return month; }
    public BigDecimal limitAmount() { return limitAmount; }
}