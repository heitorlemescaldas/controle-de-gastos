package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Expense;

public interface ExpenseRepositoryPort {
    Expense save(Expense expense);

    // novo e usado para bloquear exclusão de categoria em uso
    boolean existsByUserAndCategory(String userId, String categoryId);
}