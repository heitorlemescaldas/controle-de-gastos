package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@Tag("TDD")
class CategoryServiceTddTest {

    CategoryRepositoryPort repo;
    CategoryService sut;

    @BeforeEach
    void setup() {
        repo = mock(CategoryRepositoryPort.class);
        sut  = new CategoryService(repo);
    }

    // C01/US02: Criar categoria com sucesso #18
    @Test
    @DisplayName("C01/US02 - Deve criar categoria raiz com nome normalizado e retornar com id")
    void shouldCreateRootCategorySuccessfully() {
        var userId = "user-1";
        var input  = Category.root(userId, "  Alimentação  ");

        // quando salvar, devolve com id
        when(repo.save(any(Category.class))).thenAnswer(inv -> {
            var arg = (Category) inv.getArgument(0);
            // garante que o Service já enviou o nome trimado ao repositorio
            assertThat(arg.name()).isEqualTo("Alimentação");
            return arg.withId("cat-1");
        });

        var saved = sut.create(input);

        assertThat(saved.id()).isEqualTo("cat-1");
        assertThat(saved.userId()).isEqualTo(userId);
        assertThat(saved.name()).isEqualTo("Alimentação");
        assertThat(saved.parentId()).isNull();

        verify(repo).save(any(Category.class));
    }
}
