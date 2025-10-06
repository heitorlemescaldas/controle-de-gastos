package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    // C04/US02: Impedir criação de subcategoria se categoria raiz não existir #26
    @Test
    @DisplayName("C04/US02 - Deve rejeitar criação de subcategoria quando parent não existe")
    void shouldRejectChildCreationWhenParentDoesNotExist() {
        var userId   = "user-1";
        var parentId = "cat-missing";
        var input    = Category.child(userId, "Transporte", parentId);

        // parent NÃO existe
        when(repo.existsByIdAndUser(parentId, userId)).thenReturn(false);

        assertThatThrownBy(() -> sut.create(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("categoria pai inexistente");

        // não deve tentar salvar
        verify(repo, never()).save(any());
    }

    // C05/US02: Impedir exclusão se categoria tiver subcategorias ou estiver em uso #20
    @Test
    @DisplayName("C05/US02 - Deve impedir exclusão quando categoria possui subcategorias")
    void shouldRejectDeletionWhenCategoryHasChildren() {
        var userId = "user-1";
        var catId  = "cat-1";

        // mocks específicos para este cenário
        ExpenseRepositoryPort expenseRepo = mock(ExpenseRepositoryPort.class);
        CategoryService deleteSut = new CategoryService(repo, expenseRepo);

        when(repo.hasChildren(catId, userId)).thenReturn(true);      // possui filhos
        when(expenseRepo.existsByUserAndCategory(userId, catId)).thenReturn(false);

        assertThatThrownBy(() -> deleteSut.delete(catId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("possui subcategorias");

        verify(repo, never()).delete(anyString(), anyString());
    }

    @Test
    @DisplayName("C05/US02 - Deve impedir exclusão quando categoria está em uso por despesas")
    void shouldRejectDeletionWhenCategoryIsInUse() {
        var userId = "user-1";
        var catId  = "cat-2";

        ExpenseRepositoryPort expenseRepo = mock(ExpenseRepositoryPort.class);
        CategoryService deleteSut = new CategoryService(repo, expenseRepo);

        when(repo.hasChildren(catId, userId)).thenReturn(false);     // não tem filhos
        when(expenseRepo.existsByUserAndCategory(userId, catId)).thenReturn(true); // em uso

        assertThatThrownBy(() -> deleteSut.delete(catId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("categoria em uso");

        verify(repo, never()).delete(anyString(), anyString());
    }

    // C07/US02: Renomear categoria e verificar a atualização em cascata #21
    @Test
    @DisplayName("C07/US02 - Deve renomear categoria raiz e atualizar paths descendentes (prefix swap)")
    void shouldRenameRootAndCascadePathUpdate() {
        var userId = "user-1";
        var catId  = "cat-root";

        // path atual da raiz
        when(repo.findPathById(catId, userId)).thenReturn("Alimentação");

        // SUT usa apenas repo aqui
        sut.rename(catId, userId, "  Comida  ");

        // 1) deve salvar o novo nome + novo path da própria categoria
        verify(repo).rename(catId, userId, "Comida", "Comida");

        // 2) deve atualizar descendentes trocando prefixo "Alimentação/" -> "Comida/"
        verify(repo).updatePathPrefix(userId, "Alimentação/", "Comida/");
    }

    // C08/US02: Impedir mudança de nome para um caminho já existente #28
    @Test
    @DisplayName("C08/US02 - Deve bloquear rename quando novo path já existe")
    void shouldRejectRenameWhenNewPathAlreadyExists() {
        var userId = "user-1";
        var catId  = "cat-root";

        // path atual da raiz
        when(repo.findPathById(catId, userId)).thenReturn("Alimentação");

        // renomear para "Comida" => novo path "Comida" (conflito)
        when(repo.existsByUserAndPath(userId, "Comida")).thenReturn(true);

        assertThatThrownBy(() -> sut.rename(catId, userId, "  Comida  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("caminho já existe");

        // não deve tentar renomear nem atualizar prefixo
        verify(repo, never()).rename(anyString(), anyString(), anyString(), anyString());
        verify(repo, never()).updatePathPrefix(anyString(), anyString(), anyString());
    }

    // C09/US02: Renomear subcategoria atualiza o caminho completo #29
    @Test
    @DisplayName("C09/US02 - Deve renomear subcategoria e atualizar caminho completo e descendentes")
    void shouldRenameChildAndUpdateFullPath() {
        var userId  = "user-1";
        var childId = "cat-child";

        // path atual do filho
        when(repo.findPathById(childId, userId)).thenReturn("Alimentação/Mercado");

        // novo path NÃO conflita (C08 já cobre conflito)
        when(repo.existsByUserAndPath(userId, "Alimentação/Supermercado")).thenReturn(false);

        // ação
        sut.rename(childId, userId, "  Supermercado ");

        // verifica rename do próprio nó (nome trimado + path novo)
        verify(repo).rename(childId, userId, "Supermercado", "Alimentação/Supermercado");

        // verifica cascata nos descendentes: troca de prefixo
        verify(repo).updatePathPrefix(userId, "Alimentação/Mercado/", "Alimentação/Supermercado/");
    }

    // nomes com caracteres proibidos ou inválidos (após trim)
    @ParameterizedTest
    @ValueSource(strings = { "Alim/entação", "Comida*", "Mercado|", ":", "\\", "🍕" })
    @DisplayName("C11/US02 - create: deve rejeitar nome inválido (caracteres proibidos)")
    void shouldRejectInvalidNameOnCreate(String invalid) {
        var input = Category.root("user-1", invalid);

        assertThatThrownBy(() -> sut.create(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome inválido");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("C11/US02 - create: deve rejeitar nome muito longo (>50)")
    void shouldRejectTooLongNameOnCreate() {
        var longName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 51 'a'
        var input = Category.root("user-1", longName);

        assertThatThrownBy(() -> sut.create(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome inválido");

        verify(repo, never()).save(any());
    }

    // rename com nomes inválidos
    @ParameterizedTest
    @ValueSource(strings = { "Alim/entação", "Comida*", "Mercado|", ":", "\\", "🍕" })
    @DisplayName("C11/US02 - rename: deve rejeitar novo nome inválido (caracteres proibidos)")
    void shouldRejectInvalidNameOnRename(String invalid) {
        var userId = "user-1";
        var catId  = "cat-x";

        when(repo.findPathById(catId, userId)).thenReturn("Alimentação"); // path atual

        assertThatThrownBy(() -> sut.rename(catId, userId, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome inválido");

        verify(repo, never()).rename(anyString(), anyString(), anyString(), anyString());
        verify(repo, never()).updatePathPrefix(anyString(), anyString(), anyString());
    }

}
