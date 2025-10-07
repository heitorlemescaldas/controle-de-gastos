package br.ifsp.demo.infra.persistence.adapter;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import br.ifsp.demo.infra.persistence.repo.ExpenseJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
@Transactional
public class ExpenseJpaAdapter implements ExpenseRepositoryPort {

    private final ExpenseJpaRepository repo;

    public ExpenseJpaAdapter(ExpenseJpaRepository repo) {
        this.repo = repo;
    }

    private static ExpenseEntity toEntity(Expense d) {
        return new ExpenseEntity(
                d.id(),
                d.userId(),
                d.amount(),
                d.type(),
                d.description(),
                d.occurredAt(),
                d.categoryId()
        );
    }

    private static Expense toDomain(ExpenseEntity e) {
        // Usa factory + withId (construtor do domínio não é público)
        Expense base = Expense.of(
                e.getUserId(),
                e.getAmount(),
                e.getType(),
                e.getDescription(),
                e.getTimestamp(),
                e.getCategoryId()
        );
        return base.withId(e.getId());
    }

    @Override
    public Expense save(Expense expense) {
        ExpenseEntity saved = repo.save(toEntity(expense));
        return expense.withId(saved.getId());
    }

    @Override
    public boolean existsByUserAndCategory(String userId, String categoryId) {
        return repo.existsByUserAndCategory(userId, categoryId);
    }

    @Override
    public List<Expense> findByUserAndPeriod(String userId, Instant start, Instant end) {
        return repo.findByUserAndPeriod(userId, start, end)
                   .stream()
                   .map(ExpenseJpaAdapter::toDomain)
                   .toList();
    }
}