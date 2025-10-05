package br.ifsp.demo.domain.model;

import java.util.Objects;

public final class Category {
    private final String id;
    private final String userId;
    private final String name;
    private final String parentId; // null para raiz

    private Category(String id, String userId, String name, String parentId) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.parentId = parentId;
    }

    // fábrica para raiz
    public static Category root(String userId, String name) {
        return new Category(null, userId, name, null);
    }

    // fábrica para subcategoria (usaremos nos próximos cenários)
    public static Category child(String userId, String name, String parentId) {
        return new Category(null, userId, name, parentId);
    }

    public Category withId(String id) { return new Category(id, userId, name, parentId); }

    // usado pelo Service para normalizar nome
    public Category withName(String newName) { return new Category(id, userId, newName, parentId); }

    public String id() { return id; }
    public String userId() { return userId; }
    public String name() { return name; }
    public String parentId() { return parentId; }
}
