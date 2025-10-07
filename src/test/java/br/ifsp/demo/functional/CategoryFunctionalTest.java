package br.ifsp.demo.functional;

import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
@Tag("Functional")
class CategoryFunctionalTest {

    @Test
    @DisplayName("FT03 - Renomear categoria RAIZ deve atualizar o prefixo de TODA a árvore")
    void renameRootUpdatesTreePrefix() {
        var userId = "user-1";
        var rootId = "cat-root";

        var repo = mock(CategoryRepositoryPort.class);
        var sut  = new CategoryService(repo); // delete/move não usados aqui

        // path atual da raiz
        when(repo.findPathById(rootId, userId)).thenReturn("Alimentação");
        // novo path NÃO existe ainda
        when(repo.existsByUserAndPath(userId, "Comida")).thenReturn(false);

        // ação: renomeando com espaços (service deve fazer trim)
        sut.rename(rootId, userId, "  Comida  ");

        // verifica chamadas esperadas
        verify(repo).findPathById(rootId, userId);
        verify(repo).existsByUserAndPath(userId, "Comida");
        // rename da própria raiz (name e path)
        verify(repo).rename(rootId, userId, "Comida", "Comida");
        // atualização em cascata dos filhos: "Alimentação/..." -> "Comida/..."
        verify(repo).updatePathPrefix(userId, "Alimentação/", "Comida/");

        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("FT03b - Bloqueia rename quando o novo caminho já existe")
    void renameRootBlocksWhenNewPathAlreadyExists() {
        var userId = "user-1";
        var rootId = "cat-root";

        var repo = mock(CategoryRepositoryPort.class);
        var sut  = new CategoryService(repo);

        when(repo.findPathById(rootId, userId)).thenReturn("Alimentação");
        // novo caminho já existe para o usuário
        when(repo.existsByUserAndPath(userId, "Comida")).thenReturn(true);

        assertThatThrownBy(() -> sut.rename(rootId, userId, "Comida"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("caminho já existe");

        // não deve renomear nem atualizar prefixo
        verify(repo, never()).rename(any(), any(), any(), any());
        verify(repo, never()).updatePathPrefix(any(), any(), any());
    }
}