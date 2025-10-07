package br.ifsp.demo.functional;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;
import br.ifsp.demo.domain.service.GoalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("Functional")
class GoalFunctionalTest {

    @Test
    @DisplayName("FT01 - evaluateMonthly: gasto == limite -> exceeded=false e diff=0")
    void evaluateExactlyAtLimit() {
        var user = "u1";
        var root = "cat-food";
        var ym   = YearMonth.of(2025, 10);

        var goalRepo     = mock(GoalRepositoryPort.class);
        var categoryRepo = mock(CategoryRepositoryPort.class);
        var expenseRepo  = mock(ExpenseRepositoryPort.class);

        var sut = new GoalService(goalRepo, categoryRepo, expenseRepo);

        // meta mensal = 200
        when(goalRepo.findByUserAndCategoryAndMonth(user, root, ym))
                .thenReturn(Optional.of(Goal.monthly(user, root, ym, new BigDecimal("200.00"))));

        // path atual da raiz
        when(categoryRepo.findPathById(root, user)).thenReturn("Alimentação");

        // dois débitos dentro da árvore totalizando 200
        var e1 = Expense.of(user, new BigDecimal("120.00"), ExpenseType.DEBIT, "x",
                Instant.parse("2025-10-02T00:00:00Z"), root);
        var e2 = Expense.of(user, new BigDecimal("80.00"), ExpenseType.DEBIT, "y",
                Instant.parse("2025-10-10T00:00:00Z"), root);

        when(expenseRepo.findByUserAndPeriod(eq(user), any(), any()))
                .thenReturn(List.of(e1, e2));

        var ev = sut.evaluateMonthly(user, root, ym);

        assertThat(ev.spent()).isEqualByComparingTo("200.00");
        assertThat(ev.limit()).isEqualByComparingTo("200.00");
        assertThat(ev.exceeded()).isFalse();
        assertThat(ev.diff()).isEqualByComparingTo("0.00");

        verify(goalRepo).findByUserAndCategoryAndMonth(user, root, ym);
        verify(expenseRepo).findByUserAndPeriod(eq(user), any(), any());
        verify(categoryRepo, atLeastOnce()).findPathById(anyString(), eq(user));
    }
}
