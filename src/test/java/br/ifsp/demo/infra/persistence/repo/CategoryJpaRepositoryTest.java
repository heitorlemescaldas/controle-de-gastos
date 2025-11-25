package br.ifsp.demo.infra.persistence.repo;

import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("CategoryJpaRepository Integration Tests")
public class CategoryJpaRepositoryTest {

    @Autowired
    private CategoryJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String USER_ID_1 = "user-123";
    private static final String USER_ID_2 = "user-456";

    private CategoryEntity category1;
    private CategoryEntity category2;

    @BeforeEach
    void setup() {
        category1 = new CategoryEntity("cat-1", USER_ID_1, "Electronics", null, "Electronics");
        entityManager.persist(category1);

        category2 = new CategoryEntity("cat-2", USER_ID_2, "Tools", null, "Tools");
        entityManager.persist(category2);

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        entityManager.getEntityManager().createQuery("DELETE FROM CategoryEntity").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findNodeById Tests")
    class FindNodeByIdTests {

        private static Stream<String> provideInvalidStrings() {
            return Stream.of(
                    null,
                    "",
                    " ",
                    " \t\n "
            );
        }

        private static Stream<String> provideCommonInvalidValues() {
            return Stream.of(
                    null,
                    "",
                    " "
            );
        }

        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        @Test
        @DisplayName("Should Return Category Node When Exists And Matches User Id")
        void shouldReturnCategoryNodeWhenExistsAndMatchesUserId() {
            Optional<CategoryNode> foundNode = repository.findNodeById(category1.getId(), USER_ID_1);

            assertThat(foundNode).isPresent();
            CategoryNode node = foundNode.get();
            assertThat(node.id()).isEqualTo(category1.getId());
            assertThat(node.userId()).isEqualTo(USER_ID_1);
            assertThat(node.name()).isEqualTo("Electronics");
            assertThat(node.parentId()).isNull();
            assertThat(node.path()).isEqualTo("Electronics");
            assertThat(node).isInstanceOf(CategoryNode.class);
        }

        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        @Test
        @DisplayName("Should Return Empty Optional When Node Does Not Exist")
        void shouldReturnEmptyOptionalWhenNodeDoesNotExist() {
            String nonExistentId = "non-existent-id";

            Optional<CategoryNode> foundNode = repository.findNodeById(nonExistentId, USER_ID_1);

            assertThat(foundNode).isEmpty();
        }

        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        @Test
        @DisplayName("Should Return Empty Optional When Node Exists But User Id Does Not Match (Security Check)")
        void shouldReturnEmptyOptionalWhenNodeExistsButUserIdDoesNotMatch() {

            Optional<CategoryNode> foundNode = repository.findNodeById(category2.getId(), USER_ID_1);

            assertThat(foundNode).isEmpty();
        }

        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        @ParameterizedTest(name = "[Invalid ID] ID={0}")
        @MethodSource("provideInvalidStrings")
        @DisplayName("Should Return Empty Optional when Category ID is Invalid (null, empty, or blank)")
        void shouldReturnEmptyOptionalWhenIdIsInvalid(String invalidId) {
            Optional<CategoryNode> foundNode = repository.findNodeById(invalidId, USER_ID_1);

            assertThat(foundNode).isEmpty();
        }

        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        @ParameterizedTest(name = "[Invalid User ID] User ID={0}")
        @MethodSource("provideInvalidStrings")
        @DisplayName("Should Return Empty Optional when User ID is Invalid (null, empty, or blank)")
        void shouldReturnEmptyOptionalWhenUserIdIsInvalid(String invalidUserId) {
            Optional<CategoryNode> foundNode = repository.findNodeById(category1.getId(), invalidUserId);

            assertThat(foundNode).isEmpty();
        }

        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        @ParameterizedTest(name = "[Both Invalid] ID={0} and User ID={1}")
        @MethodSource("provideCommonInvalidValues")
        @DisplayName("Should Return Empty Optional when both Category ID and User ID are Invalid (matching null/empty/blank)")
        void shouldReturnEmptyOptionalWhenIdAndUserIdAreInvalid(String invalidValue) {
            Optional<CategoryNode> foundNode = repository.findNodeById(invalidValue, invalidValue);

            assertThat(foundNode).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllOrdered Tests")
    class FindAllOrderedTests {

        @Test
        @DisplayName("Should return all categories ordered by path for given user")
        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        void shouldReturnAllCategoriesOrderedByPathForUser() {
            CategoryEntity child1 = new CategoryEntity("c1", USER_ID_1, "Phones", "cat-1", "Electronics/Phones");
            CategoryEntity child2 = new CategoryEntity("c2", USER_ID_1, "Laptops", "cat-1", "Electronics/Laptops");
            entityManager.persist(child1);
            entityManager.persist(child2);
            entityManager.flush();
            entityManager.clear();

            var list = repository.findAllOrdered(USER_ID_1);

            assertThat(list).hasSize(3);
            assertThat(list.get(0).getPath()).isEqualTo("Electronics");
            assertThat(list.get(1).getPath()).isEqualTo("Electronics/Laptops");
            assertThat(list.get(2).getPath()).isEqualTo("Electronics/Phones");
        }

        @Test
        @DisplayName("Should return empty list when user has no categories")
        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        void shouldReturnEmptyListWhenUserHasNoCategories() {
            var list = repository.findAllOrdered("another-user");
            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("rename Tests")
    class RenameTests {

        @Test
        @DisplayName("Should rename category when id and user match")
        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        void shouldRenameWhenValid() {
            int updated = repository.rename(category1.getId(), USER_ID_1, "NewName", "NewName");
            entityManager.flush();
            entityManager.clear();

            assertThat(updated).isEqualTo(1);

            CategoryEntity updatedEntity =
                    entityManager.find(CategoryEntity.class, category1.getId());

            assertThat(updatedEntity.getName()).isEqualTo("NewName");
            assertThat(updatedEntity.getPath()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("Should NOT rename when userId does not match")
        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        void shouldNotRenameWhenUserDoesNotMatch() {
            int updated = repository.rename(category1.getId(), USER_ID_2, "X", "X");
            assertThat(updated).isEqualTo(0);
        }

        @Test
        @DisplayName("Should NOT rename when id does not exist")
        @Tag("IntegrationTest")
        @Tag("PersistenceTest")
        void shouldNotRenameWhenIdDoesNotExist() {
            int updated = repository.rename("invalid-id", USER_ID_1, "X", "X");
            assertThat(updated).isEqualTo(0);
        }
    }
}
