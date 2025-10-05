package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    // C02/US02: Criar subcategoria com sucesso #24
    @Test
    @DisplayName("C02/US02 - Deve criar subcategoria com nome normalizado, vinculada ao parent")
    void shouldCreateChildCategorySuccessfully() {
        var userId   = "user-1";
        var parentId = "cat-root";
        var input    = Category.child(userId, "  Supermercado  ", parentId);

        // parent existe (sucesso)
        when(repo.existsByIdAndUser(parentId, userId)).thenReturn(true);

        // save deve receber já trimadp e com parentId correto
        when(repo.save(any(Category.class))).thenAnswer(inv -> {
            var arg = (Category) inv.getArgument(0);
            assertThat(arg.name()).isEqualTo("Supermercado");
            assertThat(arg.parentId()).isEqualTo(parentId);
            assertThat(arg.userId()).isEqualTo(userId);
            return arg.withId("cat-2");
        });

        var saved = sut.create(input);

        assertThat(saved.id()).isEqualTo("cat-2");
        assertThat(saved.name()).isEqualTo("Supermercado");
        assertThat(saved.parentId()).isEqualTo(parentId);

        // garante que o servico verificou a existencia do parent
        verify(repo).existsByIdAndUser(parentId, userId);
        verify(repo).save(any(Category.class));
    }

    // C03/US02: Impedir duplicidade de categoria (case-insensitive + trim) #25
    @Test
    @DisplayName("C03/US02 - Deve impedir duplicidade de categoria raiz (ignora case e trim)")
    void shouldRejectDuplicateRootNameIgnoringCaseAndTrim() {
        var userId = "user-1";
        var input  = Category.root(userId, "  ALIMENTAÇÃO  ");

        // o service normaliza para "alimentação" e pergunta ao repo por (userId, parentId=null, normalizedName)
        when(repo.existsByUserAndParentAndNameNormalized(userId, null, "alimentação")).thenReturn(true);

        assertThatThrownBy(() -> sut.create(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria duplicada");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("C03/US02 - Deve impedir duplicidade de subcategoria entre irmãos (ignora case e trim)")
    void shouldRejectDuplicateChildNameIgnoringCaseAndTrim() {
        var userId   = "user-1";
        var parentId = "cat-root";
        var input    = Category.child(userId, "  MeRcAdO  ", parentId);

        // parent existe (happy path para parent)
        when(repo.existsByIdAndUser(parentId, userId)).thenReturn(true);

        // normalizado vira "mercado"
        when(repo.existsByUserAndParentAndNameNormalized(userId, parentId, "mercado")).thenReturn(true);

        assertThatThrownBy(() -> sut.create(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria duplicada");

        verify(repo, never()).save(any());
    }
}
