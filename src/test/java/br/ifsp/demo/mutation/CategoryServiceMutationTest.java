package br.ifsp.demo.mutation;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("Mutation")
@Tag("UnitTest")
class CategoryServiceMutationTest {

    private CategoryRepositoryPort repo;
    private CategoryService sut;

    @BeforeEach
    void setup() {
        repo = mock(CategoryRepositoryPort.class);
        sut = new CategoryService(repo);
    }

    @Test
    @DisplayName("Mutation - ensureValidName: Deve aceitar nome com exatamente 50 caracteres")
    void shouldAcceptNameWithExactlyMaxCharacters() {
        var validName = "a".repeat(50);
        var category = Category.root("user-1", validName);

        assertThatCode(() -> sut.create(category))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Mutation - depthOf: Deve ignorar segmentos vazios no path ao mover categoria")
    void moveShouldIgnoreEmptyPathSegmentsWhenCalculatingDepth() {
        var userId = "user-1";
        var childId = "cat-child";
        var newParentId = "cat-new-parent";

        when(repo.findPathById(childId, userId)).thenReturn("Old/Path");
        when(repo.existsByIdAndUser(newParentId, userId)).thenReturn(true);
        when(repo.findPathById(newParentId, userId)).thenReturn("A//B");

        assertThatCode(() -> sut.move(childId, newParentId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Mutation - move: Deve permitir mover categoria para a profundidade máxima exata")
    void moveShouldAllowMoveToExactMaxDepth() {
        var userId = "user-1";
        var childId = "cat-child";
        var newParentId = "cat-new-parent";

        when(repo.findPathById(childId, userId)).thenReturn("Old/Path");
        when(repo.existsByIdAndUser(newParentId, userId)).thenReturn(true);
        when(repo.findPathById(newParentId, userId)).thenReturn("A/B");

        assertThatCode(() -> sut.move(childId, newParentId, userId))
                .doesNotThrowAnyException();
    }
}