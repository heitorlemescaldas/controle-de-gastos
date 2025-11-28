package br.ifsp.demo.infra.persistence.repo;

import br.ifsp.demo.infra.persistence.entity.GoalEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("GoalJpaRepository Integration Tests")
public class GoalJpaRepositoryTest {

    @Autowired
    private GoalJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String USER_A = "user-A";
    private static final String USER_B = "user-B";
    private static final String USER_C_NON_EXISTENT = "user-C-non-existent";

    private static final String CAT_X = "cat-X";
    private static final String CAT_Y = "cat-Y";
    private static final String CAT_Z_NON_EXISTENT = "cat-Z-non-existent";

    private static final String MONTH_JAN = "2025-01";
    private static final String MONTH_FEB = "2025-02";
    private static final String MONTH_MAR_NON_EXISTENT = "2025-03";

    private static final BigDecimal GOAL_AMOUNT = new BigDecimal("500.00");

    @BeforeEach
    void setup() {
        entityManager.persist(new GoalEntity("goal-1", USER_A, CAT_X, MONTH_JAN, GOAL_AMOUNT));
        entityManager.persist(new GoalEntity("goal-2", USER_B, CAT_Y, MONTH_JAN, GOAL_AMOUNT));
        entityManager.persist(new GoalEntity("goal-3", USER_A, CAT_Y, MONTH_FEB, GOAL_AMOUNT));
        entityManager.persist(new GoalEntity("goal-4", USER_A, CAT_X, MONTH_FEB, GOAL_AMOUNT));

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
    @ParameterizedTest(name = "user={0}, category={1}, month={2} => Decision={3}")
    @CsvSource({
            "true, true, true, true",
            "false, true, true, false",
            "true, false, true, false",
            "true, true, false, false"
    })
    @DisplayName("Branches for findMonthly")
    void testFindMonthlyBranches(boolean userMatch, boolean categoryMatch, boolean monthMatch, boolean expectedResult) {

        String targetUserId = userMatch ? USER_A : USER_C_NON_EXISTENT;
        String targetCategoryId = categoryMatch ? CAT_X : CAT_Z_NON_EXISTENT;
        String targetMonth = monthMatch ? MONTH_JAN : MONTH_MAR_NON_EXISTENT;

        Optional<GoalEntity> result = repository.findMonthly(targetUserId, targetCategoryId, targetMonth);

        assertThat(result.isPresent()).isEqualTo(expectedResult);

        if (expectedResult) {
            assertThat(result.get().getLimitAmount()).isEqualTo(GOAL_AMOUNT);
        }
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "Invalid User ID: {0}")
    @MethodSource("provideInvalidStrings")
    @DisplayName("Should return Optional.empty() when User ID is null, empty, or blank")
    void shouldReturnEmptyWhenUserIdIsInvalid(String invalidUserId) {
        Optional<GoalEntity> result = repository.findMonthly(invalidUserId, CAT_X, MONTH_JAN);
        assertThat(result).isEmpty();
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "Invalid Category ID: {0}")
    @MethodSource("provideInvalidStrings")
    @DisplayName("Should return Optional.empty() when Category ID is null, empty, or blank")
    void shouldReturnEmptyWhenCategoryIdIsInvalid(String invalidCategoryId) {
        Optional<GoalEntity> result = repository.findMonthly(USER_A, invalidCategoryId, MONTH_JAN);
        assertThat(result).isEmpty();
    }

    @Tag("IntegrationTest")
    @Tag("PersistenceTest")
    @ParameterizedTest(name = "Invalid Month: {0}")
    @MethodSource("provideInvalidStrings")
    @DisplayName("Should return Optional.empty() when Month is null, empty, or blank")
    void shouldReturnEmptyWhenMonthIsInvalid(String invalidMonth) {
        Optional<GoalEntity> result = repository.findMonthly(USER_A, CAT_X, invalidMonth);
        assertThat(result).isEmpty();
    }
}