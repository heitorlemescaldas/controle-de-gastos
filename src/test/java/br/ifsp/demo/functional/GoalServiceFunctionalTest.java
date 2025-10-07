package br.ifsp.demo.functional;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.service.GoalService;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
class GoalServiceCoverageTest {

    @Test
    @DisplayName("setMonthlyGoal - rejeita quando categoria não existe")
    void setMonthlyRejectsWhenCategoryNotFound() {
        var goalRepo = mock(GoalRepositoryPort.class);
        var catRepo  = mock(CategoryRepositoryPort.class);
        var sut      = new GoalService(goalRepo, catRepo);

        var user="u1"; var cat="c1"; var ym= YearMonth.of(2025,10);

        when(catRepo.existsByIdAndUser(cat, user)).thenReturn(false);

        assertThatThrownBy(() -> sut.setMonthlyGoal(user, cat, ym, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria inexistente");
    }

    @Test
    @DisplayName("evaluateMonthly - lança quando expenseRepo não está configurado")
    void evaluateThrowsWhenExpenseRepoIsNull() {
        var goalRepo = mock(GoalRepositoryPort.class);
        var catRepo  = mock(CategoryRepositoryPort.class);
        var sut      = new GoalService(goalRepo, catRepo); // expenseRepo = null

        var user="u1"; var cat="c1"; var ym= YearMonth.of(2025,10);
        when(goalRepo.findByUserAndCategoryAndMonth(user, cat, ym))
                .thenReturn(Optional.of(Goal.monthly(user, cat, ym, new BigDecimal("50.00"))));
        when(catRepo.findPathById(cat, user)).thenReturn("Alimentação");

        assertThatThrownBy(() -> sut.evaluateMonthly(user, cat, ym))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expenseRepo");
    }

    @Test
    @DisplayName("evaluateMonthly - sem transações → spent=0, exceeded=false, diff=0")
    void evaluateWithNoTransactions() {
        var goalRepo = mock(GoalRepositoryPort.class);
        var catRepo  = mock(CategoryRepositoryPort.class);
        var expRepo  = mock(ExpenseRepositoryPort.class);
        var sut      = new GoalService(goalRepo, catRepo, expRepo);

        var user="u1"; var cat="c1"; var ym= YearMonth.of(2025,10);
        when(goalRepo.findByUserAndCategoryAndMonth(user, cat, ym))
                .thenReturn(Optional.of(Goal.monthly(user, cat, ym, new BigDecimal("50.00"))));
        when(catRepo.findPathById(cat, user)).thenReturn("Alimentação");
        when(expRepo.findByUserAndPeriod(eq(user), any(), any())).thenReturn(java.util.List.of());

        var ev = sut.evaluateMonthly(user, cat, ym);
        assertThat(ev.spent()).isEqualByComparingTo("0.00");
        assertThat(ev.exceeded()).isFalse();
        assertThat(ev.diff()).isEqualByComparingTo("0.00");
    }
}