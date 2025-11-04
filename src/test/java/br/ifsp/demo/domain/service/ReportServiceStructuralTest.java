package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("Structural")
@Tag("UnitTest")
class ReportServiceStructuralTest {

    private CategoryRepositoryPort categoryRepo;
    private ReportService sut;

    @BeforeEach
    void setup() {
        ExpenseRepositoryPort expenseRepo = mock(ExpenseRepositoryPort.class);
        categoryRepo = mock(CategoryRepositoryPort.class);
        sut = new ReportService(expenseRepo, categoryRepo);
    }

    // Cobertura para linhas 63, 65 e 67
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Structural/generateForCategoryTree - Deve rejeitar userId ou rootCategoryId nulos ou vazios")
    void generateForCategoryTreeShouldRejectInvalidIds(String invalidId) {
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var end = Instant.parse("2025-01-31T23:59:59Z");

        // Testando userId inválido (linha 63)
        assertThatThrownBy(() -> sut.generateForCategoryTree(invalidId, start, end, "cat-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId obrigatório");

        // Testando rootCategoryId inválido (linha 65)
        assertThatThrownBy(() -> sut.generateForCategoryTree("user-1", start, end, invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rootCategoryId obrigatório");
    }

    // Cobertura para linha 67
    @Test
    @DisplayName("Structural/generateForCategoryTree - Deve rejeitar período inválido")
    void generateForCategoryTreeShouldRejectInvalidPeriod() {
        var validStart = Instant.parse("2025-01-01T00:00:00Z");
        var validEnd = Instant.parse("2025-01-31T23:59:59Z");

        // Testando start e end nulos
        assertThatThrownBy(() -> sut.generateForCategoryTree("user-1", null, validEnd, "cat-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período inválido");
        assertThatThrownBy(() -> sut.generateForCategoryTree("user-1", validStart, null, "cat-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período inválido");

        // Testando start depois de end
        assertThatThrownBy(() -> sut.generateForCategoryTree("user-1", validEnd, validStart, "cat-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período inválido");
    }

    // Cobertura para linha 71
    @Test
    @DisplayName("Structural/generateForCategoryTree - Deve falhar se a categoria raiz não tiver um path")
    void generateForCategoryTreeShouldFailIfRootPathIsMissing() {
        // Given
        var user = "user-1";
        var rootId = "cat-root";
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var end = Instant.parse("2025-01-31T23:59:59Z");

        // Simula inconsistência: categoria existe, mas não tem path
        when(categoryRepo.findPathById(rootId, user)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> sut.generateForCategoryTree(user, start, end, rootId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria raiz inexistente");
    }
}