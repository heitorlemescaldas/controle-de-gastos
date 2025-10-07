package br.ifsp.demo.infra.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_cat_user", columnList = "userId"),
        @Index(name = "idx_cat_parent", columnList = "parentId"),
        @Index(name = "idx_cat_user_path", columnList = "userId,path")
    }
)

public class CategoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, length = 50)
    private String name;

    // null para raiz
    @Column(length = 36)
    private String parentId;

    @Column(nullable = false, length = 255)
    private String path;

    public CategoryEntity() { }

    public CategoryEntity(String id, String userId, String name, String parentId, String path) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.parentId = parentId;
        this.path = path;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    // equals & hashCode por id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "CategoryEntity{id='%s', userId='%s', name='%s', parentId='%s', path='%s'}"
                .formatted(id, userId, name, parentId, path);
    }
}