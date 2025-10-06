package br.ifsp.demo.infra.persistence.entity;

import br.ifsp.demo.domain.model.ExpenseType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="expenses")
public class ExpenseEntity {
    @Id @Column(length=36) private String id;
    @Column(nullable=false, length=36) private String userId;
    @Column(nullable=false, precision=15, scale=2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ExpenseType type;
    @Column(nullable=false, length=120) private String description;
    @Column(nullable=false) private Instant timestamp;
    @Column(length=36) private String categoryId; // null permitido
    public ExpenseEntity() {}
    public ExpenseEntity(String id,String userId,BigDecimal amount,ExpenseType type,
                         String description,Instant timestamp,String categoryId) {
        this.id=id; this.userId=userId; this.amount=amount; this.type=type;
        this.description=description; this.timestamp=timestamp; this.categoryId=categoryId;
    }
}