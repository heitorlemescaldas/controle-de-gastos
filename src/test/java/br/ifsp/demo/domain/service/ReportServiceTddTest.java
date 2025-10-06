package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.Report;
import br.ifsp.demo.domain.model.ReportItem;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class ReportServiceTddTest {

    ExpenseRepositoryPort expenseRepo;
    CategoryRepositoryPort categoryRepo;
    ReportService sut;

    @BeforeEach
    void setup() {
        expenseRepo = mock(ExpenseRepositoryPort.class);
        categoryRepo = mock(CategoryRepositoryPort.class);
        sut = new ReportService(expenseRepo, categoryRepo);
    }

    @Test
    @DisplayName("C01/US03 - Deve gerar relatório válido para período (total débito, crédito, saldo e itens por categoria)")
    void shouldGenerateReportForPeriod() {
        var user = "user-1";
        var start = Instant.parse("2025-10-01T00:00:00Z");
        var end   = Instant.parse("2025-10-31T23:59:59Z");

        // dados de entrada (3 lançamentos)
        var e1 = Expense.of(user, new BigDecimal("50.00"), ExpenseType.DEBIT,  "Mercado",  Instant.parse("2025-10-05T12:00:00Z"), "cat-food");
        var e2 = Expense.of(user, new BigDecimal("20.00"), ExpenseType.CREDIT, "Estorno",  Instant.parse("2025-10-10T10:00:00Z"), null);
        var e3 = Expense.of(user, new BigDecimal("30.00"), ExpenseType.DEBIT,  "Ônibus",   Instant.parse("2025-10-12T08:00:00Z"), "cat-transport");

        when(expenseRepo.findByUserAndPeriod(user, start, end)).thenReturn(List.of(e1, e2, e3));

        // paths de categorias (para exibir/agrupar)
        when(categoryRepo.findPathById("cat-food", user)).thenReturn("Alimentação/Mercado");
        when(categoryRepo.findPathById("cat-transport", user)).thenReturn("Transporte");

        Report r = sut.generate(user, start, end);

        assertThat(r.userId()).isEqualTo(user);
        assertThat(r.start()).isEqualTo(start);
        assertThat(r.end()).isEqualTo(end);
        assertThat(r.totalDebit()).isEqualByComparingTo("80.00");
        assertThat(r.totalCredit()).isEqualByComparingTo("20.00");
        assertThat(r.balance()).isEqualByComparingTo("-60.00"); // credit - debit

        // itens ordenados por path
        assertThat(r.items()).extracting(ReportItem::categoryPath).containsExactly(
                "Alimentação/Mercado",
                "Sem categoria",
                "Transporte"
        );
        assertThat(r.items()).extracting(ReportItem::debit).containsExactly(
                new BigDecimal("50.00"), // food
                BigDecimal.ZERO,         // no debit in "Sem categoria" (foi credit)
                new BigDecimal("30.00")  // transport
        );
        assertThat(r.items()).extracting(ReportItem::credit).containsExactly(
                BigDecimal.ZERO,         // food
                new BigDecimal("20.00"), // sem cat (credit)
                BigDecimal.ZERO          // transport
        );

        verify(expenseRepo).findByUserAndPeriod(user, start, end);
        verify(categoryRepo, atLeastOnce()).findPathById(anyString(), eq(user));
    }
}