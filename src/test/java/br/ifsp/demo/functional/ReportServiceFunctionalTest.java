package br.ifsp.demo.functional;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.Report;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.service.ReportService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
class ReportServiceFunctionalTest {

    @Test
    @DisplayName("generate - rejeita userId nulo e período inválido")
    void generateRejectsNullUserAndInvalidPeriod() {
        var expRepo = mock(ExpenseRepositoryPort.class);
        var catRepo = mock(CategoryRepositoryPort.class);
        var sut = new ReportService(expRepo, catRepo);

        var start = Instant.parse("2025-10-31T23:59:59Z");
        var end   = Instant.parse("2025-10-01T00:00:00Z");

        assertThatThrownBy(() -> sut.generate(null, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId");
        assertThatThrownBy(() -> sut.generate("u1", start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período inválido");
    }

    @Test
    @DisplayName("generate - ignora despesas cujos paths de categoria são nulos/desconhecidos")
    void generateIgnoresUnknownCategoryPaths() {
        var expRepo = mock(ExpenseRepositoryPort.class);
        var catRepo = mock(CategoryRepositoryPort.class);
        var sut = new ReportService(expRepo, catRepo);

        var user="u1";
        var start = Instant.parse("2025-10-01T00:00:00Z");
        var end   = Instant.parse("2025-10-31T23:59:59Z");

        var known = "cat-known";
        var unknown = "cat-unknown";

        var e1 = Expense.of(user, new BigDecimal("10.00"), ExpenseType.DEBIT, "x",
                Instant.parse("2025-10-05T00:00:00Z"), known);
        var e2 = Expense.of(user, new BigDecimal("5.00"), ExpenseType.DEBIT, "y",
                Instant.parse("2025-10-06T00:00:00Z"), unknown);

        when(catRepo.findPathById(known, user)).thenReturn("Transporte");
        when(catRepo.findPathById(unknown, user)).thenReturn(null);
        when(expRepo.findByUserAndPeriod(user, start, end)).thenReturn(List.of(e1, e2));

        Report r = sut.generate(user, start, end);

        assertThat(r.items()).hasSize(1);
        assertThat(r.items().get(0).categoryPath()).isEqualTo("Transporte");
        assertThat(r.totalDebit()).isEqualByComparingTo("10.00");
    }
}