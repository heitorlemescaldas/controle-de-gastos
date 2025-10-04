package br.ifsp.demo.domain.port;

public interface CategoryRepositoryPort {
    boolean existsByIdAndUser(String categoryId, String userId);
}
