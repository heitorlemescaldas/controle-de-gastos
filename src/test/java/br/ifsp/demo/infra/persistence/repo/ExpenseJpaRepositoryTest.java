package br.ifsp.demo.infra.persistence.repo;

import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ExpenseJpaRepository Integration Tests")
public class ExpenseJpaRepositoryTest {

    @Autowired
    private ExpenseJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String USER_A = "user-A";
    private static final String USER_B = "user-B";
    private static final String CAT_X = "cat-X";
    private static final String CAT_Y = "cat-Y";

    private final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    private final Instant PAST = NOW.minus(1, ChronoUnit.DAYS);
    private final Instant FUTURE = NOW.plus(1, ChronoUnit.DAYS);

    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.TEN;
    private static final ExpenseType DEFAULT_TYPE = ExpenseType.DEBIT;
    private static final String DEFAULT_DESCRIPTION = "Test Expense";

    @BeforeEach
    void setup() {
        entityManager.persist(new ExpenseEntity("exp-1", USER_A, DEFAULT_AMOUNT, DEFAULT_TYPE, DEFAULT_DESCRIPTION, NOW, CAT_X));
        entityManager.persist(new ExpenseEntity("exp-2", USER_A, DEFAULT_AMOUNT, DEFAULT_TYPE, DEFAULT_DESCRIPTION, NOW, CAT_Y));
        entityManager.persist(new ExpenseEntity("exp-3", USER_B, DEFAULT_AMOUNT, DEFAULT_TYPE, DEFAULT_DESCRIPTION, NOW, CAT_Y));
        entityManager.persist(new ExpenseEntity("exp-4", USER_A, DEFAULT_AMOUNT, DEFAULT_TYPE, DEFAULT_DESCRIPTION, PAST, CAT_X));
        entityManager.persist(new ExpenseEntity("exp-5", USER_A, DEFAULT_AMOUNT, DEFAULT_TYPE, DEFAULT_DESCRIPTION, FUTURE, CAT_X));

        entityManager.flush();
        entityManager.clear();
    }

    private static Stream<String> provideInvalidStrings() {
        return Stream.of(
                null,
                "",
                " ",
                " \t\n "
        );
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "user={0}, category={1} => Decision={2}")
    @CsvSource({
            "true, true, true",
            "false, true, false",
            "true, false, false"
    })
    @DisplayName("Branches for existsByUserAndCategory")
    void testExistsByUserAndCategory(boolean userMatch, boolean categoryMatch, boolean expectedResult) {
        String targetUserId = userMatch ? USER_A : USER_B;
        String targetCategoryId = categoryMatch ? CAT_X : "cat-Z";

        boolean result = repository.existsByUserAndCategory(targetUserId, targetCategoryId);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "Invalid user id: {0}")
    @MethodSource("provideInvalidStrings")
    @DisplayName("Should NOT exist when User ID is null, empty, or blank")
    void shouldNotExistWhenUserIdIsInvalid(String invalidUserId) {
        boolean result = repository.existsByUserAndCategory(invalidUserId, CAT_X);

        assertThat(result).isFalse();
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "Invalid category id: {0}")
    @MethodSource("provideInvalidStrings")
    @DisplayName("Should NOT exist when Category ID is null, empty, or blank")
    void shouldNotExistWhenCategoryIdIsInvalid(String invalidCategoryId) {
        boolean result = repository.existsByUserAndCategory(USER_A, invalidCategoryId);

        assertThat(result).isFalse();
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "Invalid user id: {0}")
    @MethodSource("provideInvalidStrings")
    @DisplayName("Should return EMPTY list when User ID is null, empty, or blank in period search")
    void shouldReturnEmptyListWhenUserIdIsInvalidInPeriodSearch(String invalidUserId) {
        Instant start = PAST;
        Instant end = FUTURE;

        List<ExpenseEntity> results = repository.findByUserAndPeriod(invalidUserId, start, end);

        assertThat(results).isEmpty();
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @Test
    @DisplayName("Should find expenses when timestamp is strictly within the period")
    void shouldFindExpensesWhenTimestampIsWithinPeriod() {
        Instant start = NOW.minus(5, ChronoUnit.SECONDS);
        Instant end = NOW.plus(5, ChronoUnit.SECONDS);

        List<ExpenseEntity> expenses = repository.findByUserAndPeriod(USER_A, start, end);

        assertThat(expenses)
                .hasSize(2)
                .extracting(ExpenseEntity::getId)
                .containsExactlyInAnyOrder("exp-1", "exp-2");
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @Test
    @DisplayName("Should find expenses at boundary limits (timestamp = start or timestamp = end)")
    void shouldFindExpensesAtBoundaryLimits() {
        Instant start = NOW;
        Instant end = NOW;

        List<ExpenseEntity> expenses = repository.findByUserAndPeriod(USER_A, start, end);

        assertThat(expenses)
                .hasSize(2)
                .extracting(ExpenseEntity::getId)
                .containsExactlyInAnyOrder("exp-1", "exp-2");
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @Test
    @DisplayName("Should NOT find expenses when timestamp is before start")
    void shouldNotFindExpensesWhenTimestampIsBeforeStart() {
        Instant start = NOW.plus(1, ChronoUnit.SECONDS);
        Instant end = FUTURE.minus(1, ChronoUnit.DAYS);

        List<ExpenseEntity> expenses = repository.findByUserAndPeriod(USER_A, start, end);

        assertThat(expenses).isEmpty();
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @Test
    @DisplayName("Should NOT find expenses when timestamp is after end")
    void shouldNotFindExpensesWhenTimestampIsAfterEnd() {
        Instant start = PAST;
        Instant end = NOW.minus(1, ChronoUnit.SECONDS);

        List<ExpenseEntity> expenses = repository.findByUserAndPeriod(USER_A, start, end);

        assertThat(expenses)
                .hasSize(1)
                .extracting(ExpenseEntity::getId)
                .containsExactly("exp-4");
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @Test
    @DisplayName("Should ensure user isolation")
    void shouldEnsureUserIsolation() {
        Instant start = PAST;
        Instant end = FUTURE;

        List<ExpenseEntity> expenses = repository.findByUserAndPeriod(USER_B, start, end);

        assertThat(expenses)
                .hasSize(1)
                .extracting(ExpenseEntity::getId)
                .containsExactly("exp-3");
    }

    private final Instant START_BEFORE_NOW = NOW.minus(1, ChronoUnit.SECONDS);
    private final Instant START_AFTER_NOW = NOW.plus(1, ChronoUnit.SECONDS);
    private final Instant END_BEFORE_NOW = NOW.minus(1, ChronoUnit.SECONDS);
    private final Instant END_AFTER_NOW = NOW.plus(1, ChronoUnit.SECONDS);

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "start={0}, end={1} => Results found={2}")
    @CsvSource({
            "true, true, 2",
            "false, true, 0",
            "true, false, 0"
    })
    @DisplayName("Branches for findByUserAndPeriod")
    void testFindByUserAndPeriod(boolean meetsStart, boolean meetsEnd, int expectedSize) {

        Instant testStart = meetsStart ? START_BEFORE_NOW : START_AFTER_NOW;
        Instant testEnd = meetsEnd ? END_AFTER_NOW : END_BEFORE_NOW;

        List<ExpenseEntity> results = repository.findByUserAndPeriod(USER_A, testStart, testEnd);

        assertThat(results).hasSize(expectedSize);
    }
}