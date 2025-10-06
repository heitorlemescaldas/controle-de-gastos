package br.ifsp.demo.infra.persistence.adapter;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import br.ifsp.demo.infra.persistence.repo.ExpenseJpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public class ExpenseJpaAdapter implements ExpenseRepositoryPort {

    private final ExpenseJpaRepository repo;
    public ExpenseJpaAdapter(ExpenseJpaRepository repo){ this.repo = repo; }

    private static ExpenseEntity toEntity(Expense d){
        return new ExpenseEntity(d.id(), d.userId(), d.amount(), d.type(), d.description(), d.timestamp(), d.categoryId());
    }
    private static Expense toDomain(ExpenseEntity e){
        return new Expense(e.getId(), e.getUserId(), e.getAmount(), e.getType(), e.getDescription(), e.getTimestamp(), e.getCategoryId());
    }

    @Override public Expense save(Expense expense) {
        var e = toEntity(expense);
        repo.save(e);
        return expense.withId(e.getId());
    }
    @Override public boolean existsByUserAndCategory(String userId, String categoryId) {
        return repo.existsByUserAndCategory(userId, categoryId);
    }
    @Override public List<Expense> findByUserAndPeriod(String userId, Instant start, Instant end) {
        return repo.findByUserAndPeriod(userId, start, end).stream().map(ExpenseJpaAdapter::toDomain).toList();
    }
}