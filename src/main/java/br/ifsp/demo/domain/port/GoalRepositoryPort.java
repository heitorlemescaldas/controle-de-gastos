package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Goal;
import java.time.YearMonth;
import java.util.Optional; // <-- IMPORTANTE

public interface GoalRepositoryPort {
    Goal save(Goal goal);

    // Tipado com generics
    Optional<Goal> findByUserAndCategoryAndMonth(String userId, String rootCategoryId, YearMonth month);
}