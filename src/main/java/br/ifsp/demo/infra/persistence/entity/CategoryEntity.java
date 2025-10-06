package br.ifsp.demo.infra.persistence.entity;

import jakarta.persistence.*;

@Entity @Table(name="categories")
public class CategoryEntity {
    @Id @Column(length=36) private String id;
    @Column(nullable=false, length=36) private String userId;
    @Column(nullable=false, length=50)  private String name;
    @Column(length=36) private String parentId; // null para raiz
    @Column(nullable=false, length=255) private String path;

    // getters/setters/constructors
    public CategoryEntity() {}
    public CategoryEntity(String id, String userId, String name, String parentId, String path) {
        this.id=id; this.userId=userId; this.name=name; this.parentId=parentId; this.path=path;
    }
}