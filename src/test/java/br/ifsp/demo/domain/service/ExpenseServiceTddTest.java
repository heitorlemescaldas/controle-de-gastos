package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.Instant;

import br.ifsp.demo.domain.port.ExpenseRepositoryPort;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*; 
import br.ifsp.demo.domain.port.CategoryRepositoryPort;

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
        var amount = new BigDecimal("0.01");
        var expense = Expense.of(
                userId, amount, ExpenseType.DEBIT,
                "Almoço", Instant.parse("2025-10-01T12:00:00Z"),
                null
        );

        when(expenseRepo.save(any(Expense.class))).thenReturn(expense.withId("e-1"));

        var saved = sut.create(expense);

        assertThat(saved.id()).isEqualTo("e-1");
        assertThat(saved.userId()).isEqualTo(userId);
        assertThat(saved.amount()).isEqualByComparingTo("0.01");
        assertThat(saved.type()).isEqualTo(ExpenseType.DEBIT);
        assertThat(saved.description()).isEqualTo("Almoço");
        assertThat(saved.occurredAt()).isEqualTo(Instant.parse("2025-10-01T12:00:00Z"));
        assertThat(saved.categoryId()).isNull();
        verify(expenseRepo).save(any(Expense.class));
    }

    // C02/US01: Tentar registrar uma despesa com valor nulo ou negativo #14
    @Test
    @DisplayName("C02 - Não deve aceitar amount = 0.00")
    void shouldRejectZeroAmount() {
        var expenseRepo = this.expenseRepo;
        var sut = this.sut;

        var expense = br.ifsp.demo.domain.model.Expense.of(
                "user-1", new java.math.BigDecimal("0.00"),
                br.ifsp.demo.domain.model.ExpenseType.DEBIT,
                "Compra X", java.time.Instant.parse("2025-10-01T10:00:00Z"),
                null
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> sut.create(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valor deve ser positivo");

        org.mockito.Mockito.verify(expenseRepo, org.mockito.Mockito.never()).save(org.mockito.Mockito.any());
    }

    @Test
    @DisplayName("C02 - Não deve aceitar amount negativo")
    void shouldRejectNegativeAmount() {
        var expense = br.ifsp.demo.domain.model.Expense.of(
                "user-1", new java.math.BigDecimal("-0.01"),
                br.ifsp.demo.domain.model.ExpenseType.DEBIT,
                "Compra Y", java.time.Instant.parse("2025-10-01T10:05:00Z"),
                null
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> sut.create(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valor deve ser positivo");

        org.mockito.Mockito.verify(expenseRepo, org.mockito.Mockito.never()).save(org.mockito.Mockito.any());
    }

    @Test
    @DisplayName("C02 - Não deve aceitar amount nulo")
    void shouldRejectNullAmount() {
        var expense = br.ifsp.demo.domain.model.Expense.of(
                "user-1", null,
                br.ifsp.demo.domain.model.ExpenseType.DEBIT,
                "Compra Z", java.time.Instant.parse("2025-10-01T10:10:00Z"),
                null
        );

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> sut.create(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valor deve ser positivo");

        org.mockito.Mockito.verify(expenseRepo, org.mockito.Mockito.never()).save(org.mockito.Mockito.any());
    }

    // C03/US01: Tentar registrar uma despesa com descrição vazia ou nula #15
    @Test
    @DisplayName("C03 - Não deve aceitar descrição nula")
    void shouldRejectNullDescription() {
        var expense = Expense.of(
                "user-1", new BigDecimal("10.00"), ExpenseType.DEBIT,
                null, Instant.parse("2025-10-01T10:00:00Z"),
                null
        );

        assertThatThrownBy(() -> sut.create(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descrição obrigatória");

        verify(expenseRepo, never()).save(any());
    }

    @Test
    @DisplayName("C03 - Não deve aceitar descrição em branco")
    void shouldRejectBlankDescription() {
        var expense = Expense.of(
                "user-1", new BigDecimal("10.00"), ExpenseType.DEBIT,
                "   ", Instant.parse("2025-10-01T10:00:00Z"),
                null
        );

        assertThatThrownBy(() -> sut.create(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descrição obrigatória");

        verify(expenseRepo, never()).save(any());
    }

    // C04/US01: Tentar registrar uma despesa com categoria inexistente #16
    @Test
    @DisplayName("C04 - Não deve aceitar categoria inexistente")
    void shouldRejectWhenCategoryDoesNotExist() {
        var userId = "user-1";
        var categoryId = "cat-404";

        var expense = Expense.of(
                userId, new BigDecimal("15.00"), ExpenseType.DEBIT,
                "Mercado", Instant.parse("2025-10-01T10:00:00Z"),
                categoryId
        );

        // mock do repo de categorias
        CategoryRepositoryPort categoryRepo = mock(CategoryRepositoryPort.class);
        when(categoryRepo.existsByIdAndUser(categoryId, userId)).thenReturn(false);

        // cria um SUT específico com dois repositórios (mantém o SUT padrão para os demais testes)
        ExpenseService sutWithCategory = new ExpenseService(expenseRepo, categoryRepo);

        assertThatThrownBy(() -> sutWithCategory.create(expense))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria inexistente");

        verify(expenseRepo, never()).save(any());
    }

    // C05/US01: Registrar despesa e verificar o estado do agregado #17
    @Test
    @DisplayName("C05 - Deve normalizar descrição (trim) e retornar o agregado salvo com id")
    void shouldNormalizeDescriptionAndReturnSavedAggregate() {
        var expense = Expense.of(
                "user-1", new BigDecimal("23.90"), ExpenseType.DEBIT,
                "  Estacionamento  ", Instant.parse("2025-10-01T09:00:00Z"),
                null
        );

        // Quando o repositório for chamado, vamos validar que já veio TRIMADO
        when(expenseRepo.save(any())).thenAnswer(inv -> {
            var arg = (Expense) inv.getArgument(0);
            assertThat(arg.description()).isEqualTo("Estacionamento"); // precisa vir trimado
            return arg.withId("e-2"); // o repo retorna com id
        });

        var saved = sut.create(expense);

        assertThat(saved.id()).isEqualTo("e-2");
        assertThat(saved.description()).isEqualTo("Estacionamento");
        assertThat(saved.amount()).isEqualByComparingTo("23.90");
        assertThat(saved.userId()).isEqualTo("user-1");
    }

    // C06/US02: Excluir subcategoria vazia com sucesso (sem transações) #27
    @Test
    @DisplayName("C06/US02 - Deve excluir subcategoria quando não possui filhos nem está em uso")
    void shouldDeleteChildCategoryWhenNoChildrenAndNotInUse() {
        var userId = "user-1";
        var catId  = "cat-child";

        // usamos o mock 'repo' já da classe e criamos um mock para ExpenseRepo
        ExpenseRepositoryPort expenseRepo = mock(ExpenseRepositoryPort.class);
        CategoryService deleteSut = new CategoryService(repo, expenseRepo);

        when(repo.hasChildren(catId, userId)).thenReturn(false);                 // sem filhos
        when(expenseRepo.existsByUserAndCategory(userId, catId)).thenReturn(false); // não está em uso

        // não deve lançar exceção
        assertThatCode(() -> deleteSut.delete(catId, userId)).doesNotThrowAnyException();

        // deve checar filhos, uso e então deletar
        verify(repo).hasChildren(catId, userId);
        verify(expenseRepo).existsByUserAndCategory(userId, catId);
        verify(repo).delete(catId, userId);
    }

}
