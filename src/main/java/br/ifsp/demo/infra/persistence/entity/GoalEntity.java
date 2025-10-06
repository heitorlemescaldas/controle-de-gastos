package br.ifsp.demo.infra.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="goals")
public class GoalEntity {
    @Id @Column(length=36) private String id;
    @Column(nullable=false, length=36) private String userId;
    @Column(nullable=false, length=36) private String categoryId; // raiz
    @Column(nullable=false, length=7)  private String month;      // "YYYY-MM"
    @Column(nullable=false, precision=15, scale=2) private BigDecimal limitAmount;
    public GoalEntity() {}
    public GoalEntity(String id,String userId,String categoryId,String month,BigDecimal limitAmount){
        this.id=id; this.userId=userId; this.categoryId=categoryId; this.month=month; this.limitAmount=limitAmount;
    }
}