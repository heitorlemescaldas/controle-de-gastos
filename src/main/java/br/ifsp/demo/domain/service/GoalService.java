package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.model.GoalEvaluation;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class GoalService {

    private final GoalRepositoryPort goalRepo;
    private final CategoryRepositoryPort categoryRepo;
    private final ExpenseRepositoryPort expenseRepo;

    // Construtor legacy (C01) – sem despesas
    public GoalService(GoalRepositoryPort goalRepo, CategoryRepositoryPort categoryRepo) {
        this(goalRepo, categoryRepo, null);
    }

    // Construtor completo (C02) – com despesas
    public GoalService(GoalRepositoryPort goalRepo,
                       CategoryRepositoryPort categoryRepo,
                       ExpenseRepositoryPort expenseRepo) {
        this.goalRepo = goalRepo;
        this.categoryRepo = categoryRepo;
        this.expenseRepo = expenseRepo;
    }

    // C01 – definir meta mensal para categoria raiz
    public Goal setMonthlyGoal(String userId, String rootCategoryId, YearMonth month, BigDecimal limitAmount) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId obrigatório");
        if (rootCategoryId == null || rootCategoryId.isBlank()) throw new IllegalArgumentException("categoryId obrigatório");
        if (month == null) throw new IllegalArgumentException("mês obrigatório");
        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("limite deve ser positivo");

        if (!categoryRepo.existsByIdAndUser(rootCategoryId, userId))
            throw new IllegalArgumentException("categoria inexistente");

        String path = categoryRepo.findPathById(rootCategoryId, userId);
        if (path == null || path.isBlank() || path.contains("/"))
            throw new IllegalArgumentException("categoria deve ser raiz");

        var goal = Goal.monthly(userId, rootCategoryId, month, limitAmount);
        return goalRepo.save(goal);
    }

    // C02 – avaliar excedente no mês (soma débitos da árvore)
    public GoalEvaluation evaluateMonthly(String userId, String rootCategoryId, YearMonth month) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId obrigatório");
        if (rootCategoryId == null || rootCategoryId.isBlank()) throw new IllegalArgumentException("categoryId obrigatório");
        if (month == null) throw new IllegalArgumentException("mês obrigatório");

        Goal goal = goalRepo.findByUserAndCategoryAndMonth(userId, rootCategoryId, month)
                .orElseThrow(() -> new IllegalArgumentException("meta inexistente"));

        String rootPath = categoryRepo.findPathById(rootCategoryId, userId);
        if (rootPath == null || rootPath.isBlank())
            throw new IllegalArgumentException("categoria raiz inexistente");

        if (expenseRepo == null)
            throw new IllegalStateException("expenseRepo não configurado");

        Instant start = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end   = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);

        List<Expense> txs = expenseRepo.findByUserAndPeriod(userId, start, end);

        BigDecimal spent = BigDecimal.ZERO;
        for (Expense e : txs) {
            if (e.type() != ExpenseType.DEBIT) continue;
            if (e.categoryId() == null) continue;
            String path = categoryRepo.findPathById(e.categoryId(), e.userId());
            if (path == null || path.isBlank()) continue;
            if (path.equals(rootPath) || path.startsWith(rootPath + "/")) {
                spent = spent.add(e.amount());
            }
        }

        boolean exceeded = spent.compareTo(goal.limitAmount()) > 0;
        BigDecimal diff = exceeded ? spent.subtract(goal.limitAmount()) : BigDecimal.ZERO;

        return new GoalEvaluation(userId, rootCategoryId, month, goal.limitAmount(), spent, exceeded, diff);
    }
}