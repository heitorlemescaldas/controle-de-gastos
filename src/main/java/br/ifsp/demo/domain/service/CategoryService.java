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

        // normalizar nome (trim) antes de salvar
        var normalized = category.withName(category.name().trim());
        return repo.save(normalized);
    }
}