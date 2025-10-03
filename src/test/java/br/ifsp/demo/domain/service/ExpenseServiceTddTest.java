package br.ifsp.demo.domain.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class ExpenseServiceTddTest {

    ExpenseRepositoryPort expenseRepo;
    ExpenseService sut;

    @BeforeEach
    void setup() {
        expenseRepo = mock(ExpenseRepositoryPort.class);
        sut = new ExpenseService(expenseRepo);
    }

    // C01/US01: Registrar uma despesa com dados válidos e valor limite #13
    @Test
    @DisplayName("C01 - Deve registrar despesa válida com valor limite (0.01)")
    void shouldCreateExpenseWithValidDataAndLimitValue() {
        var userId = "user-1";
        var amount = new BigDecimal("0.01"); // valor limite > 0
        var expense = Expense.of(
                userId, amount, ExpenseType.DEBIT,
                "Almoço", Instant.parse("2025-10-01T12:00:00Z"),
                null // sem categoria
        );

        when(expenseRepo.save(expense)).thenReturn(expense.withId("e-1"));

        var saved = sut.create(expense);

        assertThat(saved.id()).isEqualTo("e-1");
        assertThat(saved.userId()).isEqualTo(userId);
        assertThat(saved.amount()).isEqualByComparingTo("0.01");
        assertThat(saved.type()).isEqualTo(ExpenseType.DEBIT);
        assertThat(saved.description()).isEqualTo("Almoço");
        assertThat(saved.occurredAt()).isEqualTo(Instant.parse("2025-10-01T12:00:00Z"));
        assertThat(saved.categoryId()).isNull();
        verify(expenseRepo).save(expense);
    }
}
