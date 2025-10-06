package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.GoalRepositoryPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
}