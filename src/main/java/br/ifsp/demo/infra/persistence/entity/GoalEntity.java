package br.ifsp.demo.infra.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "goals",
    indexes = {
        @Index(name = "idx_goal_user", columnList = "userId"),
        @Index(name = "idx_goal_user_cat_month", columnList = "userId,categoryId,month", unique = true)
    }
)
public class GoalEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String userId;

    // categoria raiz
    @Column(nullable = false, length = 36)
    private String categoryId;

    // formato "YYYY-MM"
    @Column(name = "\"month\"", nullable = false, length = 7)
    private String month;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    public GoalEntity() { }

    public GoalEntity(String id, String userId, String categoryId, String month, BigDecimal limitAmount) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    // equals & hashCode por id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoalEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "GoalEntity{id='%s', userId='%s', categoryId='%s', month='%s', limitAmount=%s}"
                .formatted(id, userId, categoryId, month, limitAmount);
    }
}