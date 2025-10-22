package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("Structural")
@Tag("UnitTest")
class CategoryServiceStructuralTest {

    private CategoryRepositoryPort repo;
    private CategoryService sut;

    @BeforeEach
    void setup() {
        repo = mock(CategoryRepositoryPort.class);
        sut = new CategoryService(repo);
    }

    @Test
    @DisplayName("Structural/create - Deve falhar se o caminho do pai for nulo ou vazio")
    void createShouldFailIfParentPathIsMissing() {
        var userId = "user-1";
        var parentId = "cat-parent";
        var input = br.ifsp.demo.domain.model.Category.child(userId, "Filho", parentId);

        when(repo.existsByIdAndUser(parentId, userId)).thenReturn(true);
        when(repo.findPathById(parentId, userId)).thenReturn(null); // Cenário de inconsistência

        assertThatThrownBy(() -> sut.create(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("caminho do parent inexistente");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Structural/delete - Deve rejeitar categoryId nulo ou vazio")
    void deleteShouldRejectInvalidCategoryId(String invalidId) {
        assertThatThrownBy(() -> sut.delete(invalidId, "user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoryId obrigatório");
    }
}