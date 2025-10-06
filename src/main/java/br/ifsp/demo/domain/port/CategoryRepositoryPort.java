package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Category;

public interface CategoryRepositoryPort {
    boolean existsByIdAndUser(String categoryId, String userId);
    Category save(Category category);
    boolean existsByUserAndParentAndNameNormalized(String userId, String parentId, String normalizedName);

    boolean hasChildren(String categoryId, String userId);
    void delete(String categoryId, String userId);

    String findPathById(String categoryId, String userId);
    void rename(String categoryId, String userId, String newName, String newPath);
    void updatePathPrefix(String userId, String oldPrefix, String newPrefix);
    boolean existsByUserAndPath(String userId, String path);

    // NOVO: mover nó para outro parent, definindo o novo path
    void move(String categoryId, String userId, String newParentId, String newPath);
}