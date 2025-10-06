package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.GoalEvaluation;
import java.time.Instant;
import java.util.List;

@Tag("UnitTest")
@Tag("TDD")
class GoalServiceTddTest {

    GoalRepositoryPort goalRepo;
    CategoryRepositoryPort categoryRepo;
    GoalService sut;

    @BeforeEach
    void setup() {
        goalRepo = mock(GoalRepositoryPort.class);
        categoryRepo = mock(CategoryRepositoryPort.class);
        sut = new GoalService(goalRepo, categoryRepo);
    }

    // C01/US04: Definir uma meta de gasto para uma categoria principal #10
    @Test
    @DisplayName("C01/US04 - Deve criar meta mensal para categoria raiz (path sem '/') e retornar com id")
    void shouldCreateMonthlyGoalForRootCategory() {
        var user  = "user-1";
        var catId = "cat-food";
        var ym    = YearMonth.of(2025, 10);
        var limit = new BigDecimal("500.00");

        // categoria existe e é RAIZ (path sem '/')
        when(categoryRepo.existsByIdAndUser(catId, user)).thenReturn(true);
        when(categoryRepo.findPathById(catId, user)).thenReturn("Alimentação");

        // repo salva e devolve com id
        when(goalRepo.save(any(Goal.class))).thenAnswer(inv -> {
            var g = (Goal) inv.getArgument(0);
            // o service deve enviar amount positivo e normalizado
            assertThat(g.limitAmount()).isEqualByComparingTo("500.00");
            return g.withId("g-1");
        });

        var saved = sut.setMonthlyGoal(user, catId, ym, limit);

        assertThat(saved.id()).isEqualTo("g-1");
        assertThat(saved.userId()).isEqualTo(user);
        assertThat(saved.categoryId()).isEqualTo(catId);
        assertThat(saved.month()).isEqualTo(ym);
        assertThat(saved.limitAmount()).isEqualByComparingTo("500.00");

        verify(categoryRepo, atLeastOnce()).existsByIdAndUser(catId, user);
        verify(categoryRepo).findPathById(catId, user);
        verify(goalRepo).save(any(Goal.class));
    }

    // C02/US04: Exceder uma meta de gasto e receber um alerta #11
    @Test
    @DisplayName("C02/US04 - Deve sinalizar excedente quando gasto mensal na árvore > limite")
    void shouldAlertWhenMonthlySpendingExceedsGoal() {
        var user  = "user-1";
        var ym    = YearMonth.of(2025, 10);
        var root  = "cat-food";

        // meta existente (limite 200)
        when(goalRepo.findByUserAndCategoryAndMonth(user, root, ym))
                .thenReturn(java.util.Optional.of(Goal.monthly(user, root, ym, new BigDecimal("200.00"))));

        // path da raiz e das subcategorias
        when(categoryRepo.findPathById(root, user)).thenReturn("Alimentação");
        var catMarket     = "cat-market";
        var catRestaurant = "cat-restaurant";
        var catTransport  = "cat-transport";
        when(categoryRepo.findPathById(catMarket, user)).thenReturn("Alimentação/Mercado");
        when(categoryRepo.findPathById(catRestaurant, user)).thenReturn("Alimentação/Restaurante");
        when(categoryRepo.findPathById(catTransport, user)).thenReturn("Transporte");

        // despesas do mês (somente débitos da árvore contam)
        var e1 = Expense.of(user, new BigDecimal("150.00"), ExpenseType.DEBIT,  "Compra mercado",
                Instant.parse("2025-10-05T12:00:00Z"), catMarket);
        var e2 = Expense.of(user, new BigDecimal("80.00"),  ExpenseType.DEBIT,  "Almoço",
                Instant.parse("2025-10-08T13:00:00Z"), catRestaurant);
        var e3 = Expense.of(user, new BigDecimal("10.00"),  ExpenseType.CREDIT, "Cashback",
                Instant.parse("2025-10-10T10:00:00Z"), root);          // crédito na árvore — ignora no gasto
        var e4 = Expense.of(user, new BigDecimal("70.00"),  ExpenseType.DEBIT,  "Ônibus",
                Instant.parse("2025-10-12T08:00:00Z"), catTransport);   // fora da árvore — ignora

        when(expenseRepo.findByUserAndPeriod(eq(user), any(), any())).thenReturn(List.of(e1, e2, e3, e4));

        // AÇÃO
        GoalEvaluation ev = sut.evaluateMonthly(user, root, ym);

        // ASSERT
        assertThat(ev.userId()).isEqualTo(user);
        assertThat(ev.categoryId()).isEqualTo(root);
        assertThat(ev.month()).isEqualTo(ym);
        assertThat(ev.limit()).isEqualByComparingTo("200.00");
        assertThat(ev.spent()).isEqualByComparingTo("230.00"); // 150 + 80
        assertThat(ev.exceeded()).isTrue();
        assertThat(ev.diff()).isEqualByComparingTo("30.00");   // 230 - 200

        verify(goalRepo).findByUserAndCategoryAndMonth(user, root, ym);
        verify(expenseRepo).findByUserAndPeriod(eq(user), any(), any());
        verify(categoryRepo, atLeastOnce()).findPathById(anyString(), eq(user));
    }
}