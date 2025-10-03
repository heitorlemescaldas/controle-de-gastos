package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;

import java.math.BigDecimal;

public class ExpenseService {

    private final ExpenseRepositoryPort expenseRepo;

    public ExpenseService(ExpenseRepositoryPort expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    public Expense create(Expense expense) {
        // validação mínima para C02
        if (expense == null) {
            throw new IllegalArgumentException("despesa obrigatória");
        }
        if (expense.amount() == null || expense.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("valor deve ser positivo");
        }

        // C01 (happy path): delega ao repositório
        return expenseRepo.save(expense);
    }
}