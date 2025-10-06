package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Expense;

import java.time.Instant;
import java.util.List;

public interface ExpenseRepositoryPort {
    Expense save(Expense expense);

    boolean existsByUserAndCategory(String userId, String categoryId);

    // novo para relatório
    List<Expense> findByUserAndPeriod(String userId, Instant startInclusive, Instant endInclusive);
}