package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;

public class ExpenseService {

    private final ExpenseRepositoryPort expenseRepo;

    public ExpenseService(ExpenseRepositoryPort expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    public Expense create(Expense expense) {
        // C01 (happy path): apenas delega ao repositório
        return expenseRepo.save(expense);
    }
}