package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;

import java.util.regex.Pattern;
import java.util.Arrays;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepositoryPort repo;
    private final ExpenseRepositoryPort expenseRepo; // pode ser null para cenários que não usam

    // C11 - validação de nome
    private static final int MAX_NAME = 50;
    // Letras (Unicode), dígitos, espaço, '_' e '-'
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N} _-]+$");

    private static void ensureValidName(String trimmed) {
        if (trimmed.length() > MAX_NAME) {
            throw new IllegalArgumentException("nome inválido: comprimento máximo é 50");
        }
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("nome inválido: caracteres proibidos");
        }
    }

    // C10 - profundidade máxima do caminho (ex.: Raiz=1, Filho=2, Neto=3)
    private static final int MAX_DEPTH = 3;

    private static int depthOf(String path) {
        if (path == null || path.isBlank()) return 0;
        return (int) Arrays.stream(path.split("/"))
                .filter(s -> s != null && !s.isBlank())
                .count();
    }

    // construtor existente (continua para C01..C03)
    public CategoryService(CategoryRepositoryPort repo) {
        this(repo, null);
    }

    @Autowired
    public CategoryService(CategoryRepositoryPort repo, ExpenseRepositoryPort expenseRepo) {
        this.repo = repo;
        this.expenseRepo = expenseRepo;
    }

    public Category create(Category category) {
        if (category == null) throw new IllegalArgumentException("categoria obrigatória");
        if (category.userId() == null) throw new IllegalArgumentException("userId obrigatório");
        if (category.name() == null || category.name().isBlank())
            throw new IllegalArgumentException("nome obrigatório");

        var trimmedName = category.name().trim();
        ensureValidName(trimmedName); // C11: nome válido
        var normalizedName = trimmedName.toLowerCase();
        var normalized = category.withName(trimmedName);

        if (normalized.parentId() != null) {
            boolean parentExists = repo.existsByIdAndUser(normalized.parentId(), normalized.userId());
            if (!parentExists) throw new IllegalArgumentException("categoria pai inexistente");
        }

        if (repo.existsByUserAndParentAndNameNormalized(
                normalized.userId(), normalized.parentId(), normalizedName)) {
            throw new IllegalArgumentException("categoria duplicada");
        }

        if (normalized.parentId() != null) {
            boolean parentExists = repo.existsByIdAndUser(normalized.parentId(), normalized.userId());
            if (!parentExists) throw new IllegalArgumentException("categoria pai inexistente");

            // C10: calcular o caminho candidato e checar profundidade
            String parentPath = repo.findPathById(normalized.parentId(), normalized.userId());
            if (parentPath == null || parentPath.isBlank()) {
                throw new IllegalStateException("caminho do parent inexistente");
            }
            String candidatePath = parentPath + "/" + normalized.name();
            if (depthOf(candidatePath) > MAX_DEPTH) {
                throw new IllegalArgumentException("profundidade máxima excedida");
            }
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
        ensureValidName(trimmed); // C11: nome válido

        var oldPath = repo.findPathById(categoryId, userId);
        if (oldPath == null || oldPath.isBlank())
            throw new IllegalStateException("caminho atual inexistente");

        int slash = oldPath.lastIndexOf('/');
        String newPath = (slash >= 0) ? oldPath.substring(0, slash + 1) + trimmed : trimmed;

        // C08: se o novo path for diferente do atual e já existir, bloquear
        if (!newPath.equals(oldPath) && repo.existsByUserAndPath(userId, newPath)) {
            throw new IllegalArgumentException("caminho já existe");
        }

        repo.rename(categoryId, userId, trimmed, newPath);
        repo.updatePathPrefix(userId, oldPath + "/", newPath + "/");
    }

    public void move(String categoryId, String newParentId, String userId) {
        if (categoryId == null || categoryId.isBlank())
            throw new IllegalArgumentException("categoryId obrigatório");
        if (newParentId == null || newParentId.isBlank())
            throw new IllegalArgumentException("newParentId obrigatório");
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId obrigatório");

        // path atual do nó a mover
        var oldPath = repo.findPathById(categoryId, userId);
        if (oldPath == null || oldPath.isBlank())
            throw new IllegalStateException("caminho atual inexistente");

        // nome atual = último segmento do path
        int slash = oldPath.lastIndexOf('/');
        String name = (slash >= 0) ? oldPath.substring(slash + 1) : oldPath;

        // novo parent deve existir
        if (!repo.existsByIdAndUser(newParentId, userId))
            throw new IllegalArgumentException("categoria pai inexistente");

        var newParentPath = repo.findPathById(newParentId, userId);
        if (newParentPath == null || newParentPath.isBlank())
            throw new IllegalStateException("caminho do parent inexistente");

        // impedir ciclo (não mover para dentro do próprio subárvore)
        if ((newParentPath + "/").startsWith(oldPath + "/"))
            throw new IllegalArgumentException("movimento inválido: criaria ciclo");

        // checar duplicidade no destino (irmãos)
        var normalizedName = name.toLowerCase();
        if (repo.existsByUserAndParentAndNameNormalized(userId, newParentId, normalizedName))
            throw new IllegalArgumentException("categoria duplicada");

        // checar profundidade máxima (C10)
        String newPath = newParentPath + "/" + name;
        if (depthOf(newPath) > MAX_DEPTH)
            throw new IllegalArgumentException("profundidade máxima excedida");

        // efetiva a troca do parent + path do nó e atualiza descendentes
        repo.move(categoryId, userId, newParentId, newPath);
        repo.updatePathPrefix(userId, oldPath + "/", newPath + "/");
    }

    public List<CategoryNode> listOrdered(String userId) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId obrigatório");
        return repo.findAllByUserOrdered(userId);
    }
}