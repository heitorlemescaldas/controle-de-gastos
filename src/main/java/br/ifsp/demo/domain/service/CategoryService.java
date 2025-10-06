package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;

public class CategoryService {

    private final CategoryRepositoryPort repo;
    private final ExpenseRepositoryPort expenseRepo; // pode ser null para cenários que não usam

    // construtor existente (continua para C01..C03)
    public CategoryService(CategoryRepositoryPort repo) {
        this(repo, null);
    }

    // novo construtor para operações que precisam checar uso em despesas
    public CategoryService(CategoryRepositoryPort repo, ExpenseRepositoryPort expenseRepo) {
        this.repo = repo;
        this.expenseRepo = expenseRepo;
    }

    public Category create(Category category) {
        if (category == null) throw new IllegalArgumentException("categoria obrigatória");
        if (category.userId() == null) throw new IllegalArgumentException("userId obrigatório");
        if (category.name() == null || category.name().isBlank())
            throw new IllegalArgumentException("nome obrigatório");

        var trimmedName    = category.name().trim();
        var normalizedName = trimmedName.toLowerCase();
        var normalized     = category.withName(trimmedName);

        if (normalized.parentId() != null) {
            boolean parentExists = repo.existsByIdAndUser(normalized.parentId(), normalized.userId());
            if (!parentExists) throw new IllegalArgumentException("categoria pai inexistente");
        }

        if (repo.existsByUserAndParentAndNameNormalized(
                normalized.userId(), normalized.parentId(), normalizedName)) {
            throw new IllegalArgumentException("categoria duplicada");
        }

        return repo.save(normalized);
    }

    // C05: exclusão com bloqueios
    public void delete(String categoryId, String userId) {
        if (categoryId == null || categoryId.isBlank()) throw new IllegalArgumentException("categoryId obrigatório");
        if (userId == null || userId.isBlank())         throw new IllegalArgumentException("userId obrigatório");

        if (repo.hasChildren(categoryId, userId)) {
            throw new IllegalStateException("categoria possui subcategorias");
        }
        if (expenseRepo != null && expenseRepo.existsByUserAndCategory(userId, categoryId)) {
            throw new IllegalStateException("categoria em uso");
        }

        repo.delete(categoryId, userId);
    }

    public void rename(String categoryId, String userId, String newName) {
        if (categoryId == null || categoryId.isBlank())
            throw new IllegalArgumentException("categoryId obrigatório");
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId obrigatório");
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("nome obrigatório");

        var trimmed = newName.trim();

        // caminho atual da categoria (ex.: "Alimentação" ou "Alimentação/Mercado")
        var oldPath = repo.findPathById(categoryId, userId);
        if (oldPath == null || oldPath.isBlank())
            throw new IllegalStateException("caminho atual inexistente");

        // substitui o último segmento do path pelo novo nome
        int slash = oldPath.lastIndexOf('/');
        String newPath = (slash >= 0) ? oldPath.substring(0, slash + 1) + trimmed : trimmed;

        // atualiza a própria categoria
        repo.rename(categoryId, userId, trimmed, newPath);

        // atualiza os descendentes trocando prefixo "oldPath/" -> "newPath/"
        repo.updatePathPrefix(userId, oldPath + "/", newPath + "/");
    }
}