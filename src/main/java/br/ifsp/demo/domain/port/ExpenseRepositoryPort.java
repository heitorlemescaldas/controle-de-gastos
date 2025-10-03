package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Expense;

public interface ExpenseRepositoryPort {
    Expense save(Expense expense);
}
