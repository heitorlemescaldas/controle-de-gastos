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

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Structural/rename - Deve rejeitar argumentos nulos ou vazios")
    void renameShouldRejectInvalidArguments(String invalidInput) {
        assertThatThrownBy(() -> sut.rename(invalidInput, "user-1", "Novo Nome"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sut.rename("cat-1", invalidInput, "Novo Nome"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sut.rename("cat-1", "user-1", invalidInput))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Structural/rename - Deve falhar se o caminho antigo for nulo")
    void renameShouldFailIfOldPathIsMissing() {
        var userId = "user-1";
        var catId = "cat-1";

        when(repo.findPathById(catId, userId)).thenReturn(null);

        assertThatThrownBy(() -> sut.rename(catId, userId, "Novo Nome"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("caminho atual inexistente");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Structural/move - Deve rejeitar argumentos nulos ou vazios")
    void moveShouldRejectInvalidArguments(String invalidInput) {
        assertThatThrownBy(() -> sut.move(invalidInput, "p2", "u1"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sut.move("c1", invalidInput, "u1"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sut.move("c1", "p2", invalidInput))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Structural/move - Deve falhar se o caminho do nó a mover for nulo")
    void moveShouldFailIfOldPathIsMissing() {
        when(repo.findPathById("c1", "u1")).thenReturn(null);
        assertThatThrownBy(() -> sut.move("c1", "p2", "u1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("caminho atual inexistente");
    }

    @Test
    @DisplayName("Structural/move - Deve falhar se o caminho do novo pai for nulo")
    void moveShouldFailIfNewParentPathIsMissing() {
        when(repo.findPathById("c1", "u1")).thenReturn("Old/Path");
        when(repo.existsByIdAndUser("p2", "u1")).thenReturn(true);
        when(repo.findPathById("p2", "u1")).thenReturn(null); // Inconsistência

        assertThatThrownBy(() -> sut.move("c1", "p2", "u1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("caminho do parent inexistente");
    }

    @Test
    @DisplayName("Structural/move - Deve rejeitar movimento que cria ciclo")
    void moveShouldRejectCyclicalMove() {
        when(repo.findPathById("c1", "u1")).thenReturn("A"); // Mover 'A'
        when(repo.existsByIdAndUser("p2", "u1")).thenReturn(true);
        when(repo.findPathById("p2", "u1")).thenReturn("A/B"); // Para dentro de 'A/B'

        assertThatThrownBy(() -> sut.move("c1", "p2", "u1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("criaria ciclo");
    }

    @Test
    @DisplayName("Structural/move - Deve rejeitar se nome duplicado no destino")
    void moveShouldRejectDuplicateNameAtDestination() {
        when(repo.findPathById("c1", "u1")).thenReturn("Old/Filho");
        when(repo.existsByIdAndUser("p2", "u1")).thenReturn(true);
        when(repo.findPathById("p2", "u1")).thenReturn("NewRoot");
        when(repo.existsByUserAndParentAndNameNormalized("u1", "p2", "filho")).thenReturn(true);

        assertThatThrownBy(() -> sut.move("c1", "p2", "u1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria duplicada");
    }

    @Test
    @DisplayName("Structural/move - Deve rejeitar se exceder profundidade máxima")
    void moveShouldRejectWhenExceedingMaxDepth() {
        when(repo.findPathById("c1", "u1")).thenReturn("Old/Filho");
        when(repo.existsByIdAndUser("p2", "u1")).thenReturn(true);
        when(repo.findPathById("p2", "u1")).thenReturn("A/B/C");

        assertThatThrownBy(() -> sut.move("c1", "p2", "u1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profundidade máxima excedida");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Structural/listOrdered - Deve rejeitar userId nulo ou vazio")
    void listOrderedShouldRejectInvalidUserId(String invalidId) {
        assertThatThrownBy(() -> sut.listOrdered(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId obrigatório");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Structural/create - Deve rejeitar categoria com nome nulo ou vazio")
    void createShouldRejectNullOrBlankName(String invalidName) {
        var category = br.ifsp.demo.domain.model.Category.root("user-1", invalidName);

        assertThatThrownBy(() -> sut.create(category))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome obrigatório");

        verify(repo, never()).save(any());
    }
}