package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;

import java.math.BigDecimal;
import java.time.YearMonth;

public class GoalService {

    private final GoalRepositoryPort goalRepo;
    private final CategoryRepositoryPort categoryRepo;

    public GoalService(GoalRepositoryPort goalRepo, CategoryRepositoryPort categoryRepo) {
        this.goalRepo = goalRepo;
        this.categoryRepo = categoryRepo;
    }

    public Goal setMonthlyGoal(String userId, String rootCategoryId, YearMonth month, BigDecimal limitAmount) {
        if (userId == null || userId.isBlank())      throw new IllegalArgumentException("userId obrigatório");
        if (rootCategoryId == null || rootCategoryId.isBlank())
            throw new IllegalArgumentException("categoryId obrigatório");
        if (month == null)                            throw new IllegalArgumentException("mês obrigatório");
        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("limite deve ser positivo");

        // categoria precisa existir e ser RAIZ (path sem '/')
        if (!categoryRepo.existsByIdAndUser(rootCategoryId, userId))
            throw new IllegalArgumentException("categoria inexistente");

        String path = categoryRepo.findPathById(rootCategoryId, userId);
        if (path == null || path.isBlank() || path.contains("/"))
            throw new IllegalArgumentException("categoria deve ser raiz");

        var goal = Goal.monthly(userId, rootCategoryId, month, limitAmount);
        return goalRepo.save(goal);
    }
}