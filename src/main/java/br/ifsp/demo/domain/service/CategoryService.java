package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;

public class CategoryService {

    private final CategoryRepositoryPort repo;

    public CategoryService(CategoryRepositoryPort repo) {
        this.repo = repo;
    }

    public Category create(Category category) {
        if (category == null) throw new IllegalArgumentException("categoria obrigatória");
        if (category.userId() == null) throw new IllegalArgumentException("userId obrigatório");
        if (category.name() == null || category.name().isBlank())
            throw new IllegalArgumentException("nome obrigatório");

        // normalizações
        var trimmedName = category.name().trim();
        var normalizedName = trimmedName.toLowerCase();
        var normalized = category.withName(trimmedName);

        // C02: se for subcategoria, parent precisa existir
        if (normalized.parentId() != null) {
            boolean parentExists = repo.existsByIdAndUser(normalized.parentId(), normalized.userId());
            if (!parentExists) throw new IllegalArgumentException("categoria pai inexistente");
        }

        // C03: impedir duplicidade por (user, parent, nome normalizado)
        if (repo.existsByUserAndParentAndNameNormalized(
                normalized.userId(), normalized.parentId(), normalizedName)) {
            throw new IllegalArgumentException("categoria duplicada");
        }

        return repo.save(normalized);
    }
}