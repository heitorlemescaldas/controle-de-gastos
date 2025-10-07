package br.ifsp.demo.functional;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.Report;
import br.ifsp.demo.domain.model.ReportItem;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("Functional")
class ReportFunctionalTest {

    @Test
    @DisplayName("FT02 - Relatório por período com valores grandes e itens ordenados por path")
    void reportWithLargeNumbersAndSortedItems() {
        var user  = "u1";
        var start = Instant.parse("2025-10-01T00:00:00Z");
        var end   = Instant.parse("2025-10-31T23:59:59Z");

        var expenseRepo  = mock(ExpenseRepositoryPort.class);
        var categoryRepo = mock(CategoryRepositoryPort.class);
        var sut = new ReportService(expenseRepo, categoryRepo);

        // ids de categorias
        var catBakery    = "cat-bakery";
        var catTransport = "cat-transport";

        // paths de categorias usados pelo relatório
        when(categoryRepo.findPathById(catBakery, user)).thenReturn("Alimentação/Bakery");
        when(categoryRepo.findPathById(catTransport, user)).thenReturn("Transporte");

        // despesas no período (valores grandes)
        var e1 = Expense.of(user, new BigDecimal("1234567890.12"), ExpenseType.DEBIT,  "Padaria",
                Instant.parse("2025-10-05T10:00:00Z"), catBakery);
        var e2 = Expense.of(user, new BigDecimal("100.00"),       ExpenseType.CREDIT, "Bônus",
                Instant.parse("2025-10-06T10:00:00Z"), null);              // sem categoria
        var e3 = Expense.of(user, new BigDecimal("9.88"),         ExpenseType.DEBIT,  "Ônibus",
                Instant.parse("2025-10-07T10:00:00Z"), catTransport);

        when(expenseRepo.findByUserAndPeriod(user, start, end))
                .thenReturn(List.of(e1, e2, e3));

        // AÇÃO
        Report r = sut.generate(user, start, end);

        // TOTAIS
        assertThat(r.totalDebit()).isEqualByComparingTo("1234567900.00"); // 1234567890.12 + 9.88
        assertThat(r.totalCredit()).isEqualByComparingTo("100.00");
        assertThat(r.balance()).isEqualByComparingTo("-1234567800.00");   // credit - debit

        // ORDENADO por path (case-insensitive)
        assertThat(r.items()).extracting(ReportItem::categoryPath).containsExactly(
                "Alimentação/Bakery",
                "Sem categoria",
                "Transporte"
        );

        // VALORES por item
        assertThat(r.items()).extracting(ReportItem::debit).containsExactly(
                new BigDecimal("1234567890.12"),
                BigDecimal.ZERO,
                new BigDecimal("9.88")
        );
        assertThat(r.items()).extracting(ReportItem::credit).containsExactly(
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                BigDecimal.ZERO
        );

        // chamadas esperadas
        verify(expenseRepo).findByUserAndPeriod(user, start, end);
        verify(categoryRepo, atLeastOnce()).findPathById(anyString(), eq(user));
    }
}