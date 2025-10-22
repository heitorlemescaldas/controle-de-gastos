package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("Structural")
@Tag("UnitTest")
class GoalServiceStructuralTest {

    private GoalRepositoryPort goalRepo;
    private CategoryRepositoryPort categoryRepo;
    private GoalService sut;

    @BeforeEach
    void setup() {
        goalRepo = mock(GoalRepositoryPort.class);
        categoryRepo = mock(CategoryRepositoryPort.class);
        sut = new GoalService(goalRepo, categoryRepo);
    }

    @Test
    @DisplayName("Structural/setMonthlyGoal - Deve rejeitar categoria que não é raiz (path contém '/')")
    void setMonthlyGoalShouldRejectNonRootCategory() {
        var user = "user-1";
        var catId = "cat-child";
        var ym = YearMonth.of(2025, 10);
        var limit = new BigDecimal("100.00");

        when(categoryRepo.existsByIdAndUser(catId, user)).thenReturn(true);
        when(categoryRepo.findPathById(catId, user)).thenReturn("Alimentação/Mercado");

        assertThatThrownBy(() -> sut.setMonthlyGoal(user, catId, ym, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria deve ser raiz");

        verify(goalRepo, never()).save(any());
    }

    @Test
    @DisplayName("Structural/evaluateMonthly - Deve falhar se a categoria da meta não tiver um path")
    void evaluateMonthlyShouldFailIfRootCategoryPathIsMissing() {
        var user = "user-1";
        var rootId = "cat-root";
        var ym = YearMonth.of(2025, 10);
        var goal = Goal.monthly(user, rootId, ym, new BigDecimal("200.00"));

        when(goalRepo.findByUserAndCategoryAndMonth(user, rootId, ym))
                .thenReturn(Optional.of(goal));

        when(categoryRepo.findPathById(rootId, user)).thenReturn(null);

        assertThatThrownBy(() -> sut.evaluateMonthly(user, rootId, ym))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria raiz inexistente");
    }
}