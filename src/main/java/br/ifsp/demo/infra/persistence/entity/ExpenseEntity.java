package br.ifsp.demo.infra.persistence.entity;

import br.ifsp.demo.domain.model.ExpenseType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "expenses",
    indexes = {
        @Index(name = "idx_exp_user", columnList = "userId"),
        @Index(name = "idx_exp_user_ts", columnList = "userId,timestamp"),
        @Index(name = "idx_exp_cat", columnList = "categoryId")
    }
)
public class ExpenseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExpenseType type;

    @Column(nullable = false, length = 120)
    private String description;

    @Column(nullable = false)
    private Instant timestamp;

    // null permitido
    @Column(length = 36)
    private String categoryId;

    public ExpenseEntity() { }

    public ExpenseEntity(String id, String userId, BigDecimal amount, ExpenseType type,
                         String description, Instant timestamp, String categoryId) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.categoryId = categoryId;
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

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public ExpenseType getType() { return type; }
    public void setType(ExpenseType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    // equals & hashCode por id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ExpenseEntity{id='%s', userId='%s', amount=%s, type=%s, description='%s', timestamp=%s, categoryId='%s'}"
                .formatted(id, userId, amount, type, description, timestamp, categoryId);
    }
}