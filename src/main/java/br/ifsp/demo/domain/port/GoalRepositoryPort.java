package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Goal;

public interface GoalRepositoryPort {
    Goal save(Goal goal);
}