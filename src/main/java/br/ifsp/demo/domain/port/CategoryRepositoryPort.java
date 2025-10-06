package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.model.CategoryNode;

import java.util.List;

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
    void move(String categoryId, String userId, String newParentId, String newPath);

    // NOVO: retorna já ordenado por path
    List<CategoryNode> findAllByUserOrdered(String userId);
}