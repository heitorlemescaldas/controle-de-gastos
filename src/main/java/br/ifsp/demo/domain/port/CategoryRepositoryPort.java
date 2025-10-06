package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Category;

public interface CategoryRepositoryPort {
    boolean existsByIdAndUser(String categoryId, String userId);
    Category save(Category category);

    // já existente (C03)
    boolean existsByUserAndParentAndNameNormalized(String userId, String parentId, String normalizedName);

    // já existentes (C05)
    boolean hasChildren(String categoryId, String userId);
    void delete(String categoryId, String userId);

    // novos para rename/cascata (C07)
    String findPathById(String categoryId, String userId);
    void rename(String categoryId, String userId, String newName, String newPath);
    void updatePathPrefix(String userId, String oldPrefix, String newPrefix);
}