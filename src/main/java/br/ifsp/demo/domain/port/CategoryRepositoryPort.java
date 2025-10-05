package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Category;

public interface CategoryRepositoryPort {
    boolean existsByIdAndUser(String categoryId, String userId);
    Category save(Category category);

    // NOVOS para exclusão
    boolean hasChildren(String categoryId, String userId);
    void delete(String categoryId, String userId);

    // já adicionado no C03:
    boolean existsByUserAndParentAndNameNormalized(String userId, String parentId, String normalizedName);
}