package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Category;

public interface CategoryRepositoryPort {
    // já existia (usada na US-01 C04)
    boolean existsByIdAndUser(String categoryId, String userId);

    // novo para US-02
    Category save(Category category);
}