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

        // normalizar nome
        var normalized = category.withName(category.name().trim());

        // C02: se for subcategoria, parent precisa existir para o usuário
        if (normalized.parentId() != null) {
            boolean parentExists = repo.existsByIdAndUser(normalized.parentId(), normalized.userId());
            if (!parentExists) {
                // a mensagem de erro específica será cobrada no C04 (negativo);
                // por enquanto ta ok pq basta a verificação para o happy path da C02.
                throw new IllegalArgumentException("categoria pai inexistente");
            }
        }
        return repo.save(normalized);
    }
}