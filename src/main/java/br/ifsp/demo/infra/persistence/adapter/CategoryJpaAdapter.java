package br.ifsp.demo.infra.persistence.adapter;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import br.ifsp.demo.infra.persistence.repo.CategoryJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class CategoryJpaAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository repo;
    public CategoryJpaAdapter(CategoryJpaRepository repo){ this.repo = repo; }

    private static CategoryEntity toEntity(Category d){
        return new CategoryEntity(d.id(), d.userId(), d.name(), d.parentId(),
                d.parentId()==null ? d.name() : null /* path será setado por service via rename/move */);
    }
    private static CategoryNode toNode(CategoryEntity e){
        return new CategoryNode(e.getId(), e.getUserId(), e.getName(), e.getParentId(), e.getPath());
    }

    @Override public boolean existsByIdAndUser(String categoryId, String userId) {
        return repo.existsByIdAndUserId(categoryId, userId);
    }
    @Override public Category save(Category category) {
        CategoryEntity e = new CategoryEntity(category.id(), category.userId(), category.name(),
                category.parentId(),
                category.parentId()==null ? category.name() : repo.findPath(category.parentId(), category.userId())+"/"+category.name()
        );
        repo.save(e);
        return category.withId(e.getId()); // se usar UUID no app layer, ajustar aqui
    }
    @Override public boolean existsByUserAndParentAndNameNormalized(String userId, String parentId, String normalizedName) {
        return repo.existsSiblingByNormalized(userId, parentId, normalizedName);
    }
    @Override public boolean hasChildren(String categoryId, String userId) {
        return repo.hasChildren(categoryId, userId);
    }
    @Override public void delete(String categoryId, String userId) {
        repo.deleteByIdAndUserId(categoryId, userId);
    }
    @Override public String findPathById(String categoryId, String userId) {
        return repo.findPath(categoryId, userId);
    }
    @Override public void rename(String categoryId, String userId, String newName, String newPath) {
        repo.rename(categoryId, userId, newName, newPath);
    }
    @Override public void updatePathPrefix(String userId, String oldPrefix, String newPrefix) {
        repo.updatePathPrefix(userId, oldPrefix, newPrefix);
    }
    @Override public boolean existsByUserAndPath(String userId, String path) {
        return repo.existsByUserAndPath(userId, path);
    }
    @Override public void move(String categoryId, String userId, String newParentId, String newPath) {
        repo.move(categoryId, userId, newParentId, newPath);
    }
    @Override public List<CategoryNode> findAllByUserOrdered(String userId) {
        return repo.findAllOrdered(userId).stream().map(CategoryJpaAdapter::toNode).toList();
    }
}