package br.ifsp.demo.domain.port;

import br.ifsp.demo.domain.model.Category;

public interface CategoryRepositoryPort {
    // já existia (usada na US-01 C04)
    boolean existsByIdAndUser(String categoryId, String userId);

    // novo para US-02
    Category save(Category category);

    // para us-3: verificação de duplicidade por nome normalizado (lower-case + trim) no escopo (userId, parentId)
    boolean existsByUserAndParentAndNameNormalized(String userId, String parentId, String normalizedName);
}