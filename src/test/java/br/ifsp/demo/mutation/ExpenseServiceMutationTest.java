package br.ifsp.demo.mutation;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.service.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@Tag("Mutation")
@Tag("UnitTest")
class ExpenseServiceMutationTest {

    @Test
    @DisplayName("Mutation - create: Deve rejeitar categoria se CategoryRepository não estiver configurado (for nulo)")
    void createShouldRejectCategoryWhenCategoryRepoIsNull() {
        ExpenseRepositoryPort expenseRepo = mock(ExpenseRepositoryPort.class);
        ExpenseService sut = new ExpenseService(expenseRepo); // Usa o construtor public ExpenseService(ExpenseRepositoryPort expenseRepo)

        var expenseWithCategory = Expense.of(
                "user-1",
                new BigDecimal("10.00"),
                ExpenseType.DEBIT,
                "Despesa com categoria",
                Instant.now(),
                "some-category-id" // Importante: a despesa deve ter um categoryId
        );

        assertThatThrownBy(() -> sut.create(expenseWithCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria inexistente");
    }
}