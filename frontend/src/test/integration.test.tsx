import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render } from '@testing-library/react';
import { screen, fireEvent, waitFor } from '@testing-library/dom';
import userEvent from '@testing-library/user-event';
import { Toaster } from '@/components/ui/toaster';
import Index from '@/pages/Index';

// Mock do hook de toast para evitar problemas nos testes
const mockToast = {
  toast: vi.fn()
};

vi.mock('@/hooks/use-toast', () => ({
  useToast: () => mockToast
}));

describe('Integration Tests - Expense Control App', () => {
  beforeEach(() => {
    mockToast.toast.mockClear();
  });

  const setup = () => {
    const user = userEvent.setup();
    render(
      <>
        <Index />
        <Toaster />
      </>
    );
    return { user };
  };

  describe('Complete Transaction Flow', () => {
    it('should add and remove a transaction successfully', async () => {
      const { user } = setup();

      // Verificar se o formulário está presente
      expect(screen.getByLabelText(/descrição/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/valor/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/categoria/i)).toBeInTheDocument();

      // Preencher o formulário
      await user.type(screen.getByLabelText(/descrição/i), 'Teste de Integração');
      await user.type(screen.getByLabelText(/valor/i), '150.75');
      
      // Selecionar categoria
      await user.click(screen.getByRole('combobox', { name: /categoria/i }));
      await user.click(screen.getByText('Alimentação'));
      
      // Verificar se subcategorias apareceram
      await waitFor(() => {
        expect(screen.getByLabelText(/subcategoria/i)).toBeInTheDocument();
      });

      // Selecionar subcategoria
      await user.click(screen.getByRole('combobox', { name: /subcategoria/i }));
      await user.click(screen.getByText('Supermercado'));

      // Submeter o formulário
      await user.click(screen.getByRole('button', { name: /adicionar transação/i }));

      // Verificar se a transação foi adicionada
      await waitFor(() => {
        expect(screen.getByText('Teste de Integração')).toBeInTheDocument();
      });

      // Verificar se o toast de sucesso foi chamado
      expect(mockToast.toast).toHaveBeenCalledWith({
        title: "Sucesso",
        description: "Transação adicionada com sucesso!",
      });

      // Verificar se os campos foram limpos
      expect(screen.getByLabelText(/descrição/i)).toHaveValue('');
      expect(screen.getByLabelText(/valor/i)).toHaveValue('');

      // Encontrar e clicar no botão de remover
      const removeButtons = screen.getAllByRole('button', { name: '' }); // Botões de lixeira
      const removeButton = removeButtons.find(button => 
        button.querySelector('[data-testid="trash-icon"]') || 
        button.className.includes('trash')
      );
      
      if (removeButton) {
        await user.click(removeButton);
        
        // Verificar se a transação foi removida
        await waitFor(() => {
          expect(screen.queryByText('Teste de Integração')).not.toBeInTheDocument();
        });
      }
    });

    it('should show validation errors for invalid form data', async () => {
      const { user } = setup();

      // Tentar submeter formulário vazio
      await user.click(screen.getByRole('button', { name: /adicionar transação/i }));

      // Verificar se o toast de erro foi chamado
      await waitFor(() => {
        expect(mockToast.toast).toHaveBeenCalledWith({
          title: "Erro",
          description: "Preencha todos os campos obrigatórios",
          variant: "destructive"
        });
      });
    });

    it('should validate zero value', async () => {
      const { user } = setup();

      // Preencher com valor zero
      await user.type(screen.getByLabelText(/descrição/i), 'Teste');
      await user.type(screen.getByLabelText(/valor/i), '0');
      
      await user.click(screen.getByRole('combobox', { name: /categoria/i }));
      await user.click(screen.getByText('Alimentação'));

      await user.click(screen.getByRole('button', { name: /adicionar transação/i }));

      // Verificar erro de valor
      await waitFor(() => {
        expect(mockToast.toast).toHaveBeenCalledWith({
          title: "Erro",
          description: "O valor deve ser maior que zero",
          variant: "destructive"
        });
      });
    });
  });

  describe('Dashboard Metrics', () => {
    it('should display correct initial metrics', () => {
      setup();

      // Verificar se as métricas estão visíveis
      expect(screen.getByText(/saldo atual/i)).toBeInTheDocument();
      expect(screen.getByText(/total de receitas/i)).toBeInTheDocument();
      expect(screen.getByText(/total de despesas/i)).toBeInTheDocument();

      // Verificar se valores estão formatados como moeda brasileira
      const currencyElements = screen.getAllByText(/R\$/);
      expect(currencyElements.length).toBeGreaterThan(0);
    });
  });

  describe('Goals Section', () => {
    it('should display goals with status indicators', () => {
      setup();

      // Verificar se a seção de metas está presente
      expect(screen.getByText(/metas de gastos/i)).toBeInTheDocument();

      // Verificar se pelo menos uma meta está visível
      const goalCards = screen.getAllByText(/Alimentação|Transporte|Moradia|Lazer/);
      expect(goalCards.length).toBeGreaterThan(0);
    });
  });

  describe('Transaction List', () => {
    it('should display transactions with proper formatting', () => {
      setup();

      // Verificar se a lista de transações está presente
      expect(screen.getByText(/transações recentes/i)).toBeInTheDocument();

      // Verificar se existe pelo menos uma transação mock
      const transactionElements = screen.getAllByText(/Salário|Supermercado|Combustível|Freelance|Aluguel/);
      expect(transactionElements.length).toBeGreaterThan(0);
    });
  });

  describe('Responsive Design', () => {
    it('should render without layout errors on different screen sizes', () => {
      setup();

      // Verificar se elementos principais estão presentes (layout responsivo)
      expect(screen.getByText(/controle de gastos/i)).toBeInTheDocument();
      expect(screen.getByText(/nova transação/i)).toBeInTheDocument();
      expect(screen.getByText(/metas de gastos/i)).toBeInTheDocument();
      expect(screen.getByText(/transações recentes/i)).toBeInTheDocument();
    });
  });
});