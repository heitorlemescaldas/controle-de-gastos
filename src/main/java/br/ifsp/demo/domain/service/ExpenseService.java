package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepositoryPort expenseRepo;
    private final CategoryRepositoryPort categoryRepo;

    // construtor antigo
    public ExpenseService(ExpenseRepositoryPort expenseRepo) {
        this(expenseRepo, null);
    }

    // novo construtor para cenários com categoria
    public ExpenseService(ExpenseRepositoryPort expenseRepo, CategoryRepositoryPort categoryRepo) {
        this.expenseRepo = expenseRepo;
        this.categoryRepo = categoryRepo;
    }

    public Expense create(Expense expense) {
        if (expense == null) throw new IllegalArgumentException("despesa obrigatória");
        if (expense.amount() == null || expense.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("valor deve ser positivo");
        if (expense.description() == null || expense.description().isBlank())
            throw new IllegalArgumentException("descrição obrigatória");

        // C04: categoria existente (se informada)
        if (expense.categoryId() != null) {
            if (categoryRepo == null || !categoryRepo.existsByIdAndUser(expense.categoryId(), expense.userId())) {
                throw new IllegalArgumentException("categoria inexistente");
            }
        }

        // C05: normalizar descrição antes de salvar
        var normalized = expense.withDescription(expense.description().trim());

        return expenseRepo.save(normalized); // e retornar o que o repositório salvar (com id)
    }
}