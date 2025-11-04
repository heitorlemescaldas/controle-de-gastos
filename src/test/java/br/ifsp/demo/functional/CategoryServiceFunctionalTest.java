package br.ifsp.demo.functional;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.service.CategoryService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
class CategoryServiceCoverageTest {

    @Test
    @DisplayName("create - rejeita duplicidade entre irmãos (case-insensitive + trim)")
    void createRejectsDuplicateAmongSiblings() {
        var repo = mock(CategoryRepositoryPort.class);
        var sut  = new CategoryService(repo);

        var user = "u1"; var parent = "cat-root";
        when(repo.existsByIdAndUser(parent, user)).thenReturn(true);
        when(repo.existsByUserAndParentAndNameNormalized(user, parent, "mercado")).thenReturn(true);

        var child = Category.child(user, "  Mercado  ", parent);

        assertThatThrownBy(() -> sut.create(child))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicada");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("create - rejeita quando categoria pai não existe")
    void createRejectsWhenParentNotFound() {
        var repo = mock(CategoryRepositoryPort.class);
        var sut  = new CategoryService(repo);

        var user = "u1"; var parent = "cat-root";
        when(repo.existsByIdAndUser(parent, user)).thenReturn(false);

        var child = Category.child(user, "Filho", parent);

        assertThatThrownBy(() -> sut.create(child))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pai inexistente");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("delete - funciona quando não há filhos e expenseRepo é null (ramo de by-pass)")
    void deleteWorksWithNullExpenseRepo() {
        var repo = mock(CategoryRepositoryPort.class);
        var sut  = new CategoryService(repo); // expenseRepo = null

        var user = "u1"; var cat = "cat-x";
        when(repo.hasChildren(cat, user)).thenReturn(false);

        sut.delete(cat, user);

        verify(repo).delete(cat, user);
    }

    @Test
    @DisplayName("rename - mesmo nome (após trim) não consulta duplicidade; atualiza path (mesmo prefixo)")
    void renameSameNameSkipsDuplicateCheck() {
        var repo = mock(CategoryRepositoryPort.class);
        var sut  = new CategoryService(repo);

        var user = "u1"; var root = "cat-root";
        when(repo.findPathById(root, user)).thenReturn("Alimentação");

        sut.rename(root, user, "  Alimentação  ");

        // não consulta existência do novo path porque é igual ao atual
        verify(repo, never()).existsByUserAndPath(eq(user), anyString());
        // renomeia a raiz (mesmo nome/path) e atualiza prefixo (de/para iguais)
        verify(repo).rename(root, user, "Alimentação", "Alimentação");
        verify(repo).updatePathPrefix(user, "Alimentação/", "Alimentação/");
    }

    @Test
    @DisplayName("move - rejeita quando novo parent não existe")
    void moveRejectsWhenNewParentDoesNotExist() {
        var repo = mock(CategoryRepositoryPort.class);
        var expenseRepo = mock(ExpenseRepositoryPort.class); // não usado aqui
        var sut  = new CategoryService(repo, expenseRepo);

        var user = "u1"; var child = "c1"; var newParent = "p2";
        when(repo.findPathById(child, user)).thenReturn("A/B");
        when(repo.existsByIdAndUser(newParent, user)).thenReturn(false);

        assertThatThrownBy(() -> sut.move(child, newParent, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoria pai inexistente"); // <-- Correção aqui!

        verify(repo, never()).move(any(), any(), any(), any());
    }
}