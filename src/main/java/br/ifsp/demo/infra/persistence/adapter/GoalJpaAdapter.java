package br.ifsp.demo.infra.persistence.adapter;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.port.GoalRepositoryPort;
import br.ifsp.demo.infra.persistence.entity.GoalEntity;
import br.ifsp.demo.infra.persistence.repo.GoalJpaRepository;
import org.springframework.stereotype.Repository;
import java.time.YearMonth;
import java.util.Optional;

@Repository
public class GoalJpaAdapter implements GoalRepositoryPort {

    private final GoalJpaRepository repo;
    public GoalJpaAdapter(GoalJpaRepository repo){ this.repo = repo; }

    @Override public Goal save(Goal goal) {
        var ge = new GoalEntity(goal.id(), goal.userId(), goal.categoryId(), goal.month().toString(), goal.limitAmount());
        repo.save(ge);
        return goal.withId(ge.getId());
    }

    @Override public Optional<Goal> findByUserAndCategoryAndMonth(String userId, String rootCategoryId, YearMonth month) {
        return repo.findMonthly(userId, rootCategoryId, month.toString())
                .map(g -> Goal.monthly(g.getUserId(), g.getCategoryId(), YearMonth.parse(g.getMonth()), g.getLimitAmount()).withId(g.getId()));
    }
}