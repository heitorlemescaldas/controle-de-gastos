import { describe, it, expect } from 'vitest';
import { validateTransactionForm } from '@/utils/validation';
import { calculateMetrics, validateAggregateState } from '@/utils/calculations';
import { mockCategories } from '@/data/mockData';
import { Transaction, TransactionFormData } from '@/types';

/**
 * Cenários de teste baseados nas User Stories
 */
describe('User Story Scenarios', () => {
  
  describe('US-01: Registrar Nova Despesa', () => {
    
    // C03/US01: Tentar registrar uma despesa com descrição vazia ou nula
    describe('C03 - Negative: Descrição vazia ou nula', () => {
      it('should reject transaction with empty description', () => {
        const formData: TransactionFormData = {
          description: '',
          value: '100.00',
          type: 'expense',
          category: 'Alimentação',
          subcategory: 'Supermercado'
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(false);
        expect(result.errors).toContain('Descrição é obrigatória');
      });

      it('should reject transaction with null description', () => {
        const formData: TransactionFormData = {
          description: null as any,
          value: '100.00',
          type: 'expense',
          category: 'Alimentação',
          subcategory: 'Supermercado'
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(false);
        expect(result.errors).toContain('Descrição é obrigatória');
      });

      it('should reject transaction with whitespace-only description', () => {
        const formData: TransactionFormData = {
          description: '   ',
          value: '100.00',
          type: 'expense',
          category: 'Alimentação',
          subcategory: 'Supermercado'
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(false);
        expect(result.errors).toContain('Descrição é obrigatória');
      });
    });

    // C04/US01: Tentar registrar uma despesa com categoria inexistente
    describe('C04 - Negative: Categoria inexistente', () => {
      it('should reject transaction with non-existent category', () => {
        const formData: TransactionFormData = {
          description: 'Compra de teste',
          value: '100.00',
          type: 'expense',
          category: 'Categoria Inexistente',
          subcategory: ''
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(false);
        expect(result.errors).toContain('Categoria selecionada não existe');
      });

      it('should reject transaction with empty category', () => {
        const formData: TransactionFormData = {
          description: 'Compra de teste',
          value: '100.00',
          type: 'expense',
          category: '',
          subcategory: ''
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(false);
        expect(result.errors).toContain('Categoria é obrigatória');
      });
    });

    // C05/US01: Registrar despesa e verificar o estado do agregado
    describe('C05 - Happy Path: Verificar estado do agregado', () => {
      it('should maintain correct aggregate state after adding expense', () => {
        const initialTransactions: Transaction[] = [
          {
            id: '1',
            description: 'Salário',
            value: 5000,
            type: 'income',
            category: 'Salário',
            date: '2024-01-01'
          }
        ];

        // Estado inicial
        const initialMetrics = calculateMetrics(initialTransactions);
        expect(initialMetrics.income).toBe(5000);
        expect(initialMetrics.expenses).toBe(0);
        expect(initialMetrics.balance).toBe(5000);

        // Adicionar nova despesa
        const newExpense: Transaction = {
          id: '2',
          description: 'Supermercado',
          value: -200,
          type: 'expense',
          category: 'Alimentação',
          date: '2024-01-02'
        };

        const updatedTransactions = [...initialTransactions, newExpense];
        const updatedMetrics = calculateMetrics(updatedTransactions);

        // Verificar estado agregado
        expect(updatedMetrics.income).toBe(5000);
        expect(updatedMetrics.expenses).toBe(200);
        expect(updatedMetrics.balance).toBe(4800);

        // Validar integridade do estado
        const isValidState = validateAggregateState(updatedTransactions, 5000, 200);
        expect(isValidState).toBe(true);
      });

      it('should maintain correct aggregate state after multiple transactions', () => {
        const transactions: Transaction[] = [
          { id: '1', description: 'Salário', value: 5000, type: 'income', category: 'Salário', date: '2024-01-01' },
          { id: '2', description: 'Freelance', value: 1500, type: 'income', category: 'Trabalho', date: '2024-01-02' },
          { id: '3', description: 'Supermercado', value: -300, type: 'expense', category: 'Alimentação', date: '2024-01-03' },
          { id: '4', description: 'Combustível', value: -150, type: 'expense', category: 'Transporte', date: '2024-01-04' },
          { id: '5', description: 'Cinema', value: -50, type: 'expense', category: 'Lazer', date: '2024-01-05' }
        ];

        const metrics = calculateMetrics(transactions);
        
        // Verificações dos agregados
        expect(metrics.income).toBe(6500); // 5000 + 1500
        expect(metrics.expenses).toBe(500); // 300 + 150 + 50
        expect(metrics.balance).toBe(6000); // 6500 - 500

        // Validar integridade
        const isValidState = validateAggregateState(transactions, 6500, 500);
        expect(isValidState).toBe(true);
      });
    });
  });

  describe('US-02: Categorização customizável', () => {
    
    // C01/US02: Criar categoria com sucesso
    describe('C01 - Happy Path: Criar categoria', () => {
      it('should accept transaction with valid category', () => {
        const formData: TransactionFormData = {
          description: 'Compra de material',
          value: '150.00',
          type: 'expense',
          category: 'Alimentação', // Categoria existente
          subcategory: ''
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(true);
        expect(result.errors).toHaveLength(0);
      });
    });

    // C02/US02: Criar subcategoria com sucesso
    describe('C02 - Alternative: Criar subcategoria', () => {
      it('should accept transaction with valid subcategory', () => {
        const formData: TransactionFormData = {
          description: 'Compra no supermercado',
          value: '200.00',
          type: 'expense',
          category: 'Alimentação',
          subcategory: 'Supermercado' // Subcategoria válida para Alimentação
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(true);
        expect(result.errors).toHaveLength(0);
      });

      it('should reject transaction with invalid subcategory for category', () => {
        const formData: TransactionFormData = {
          description: 'Compra teste',
          value: '100.00',
          type: 'expense',
          category: 'Alimentação',
          subcategory: 'Combustível' // Subcategoria não pertence a Alimentação
        };

        const result = validateTransactionForm(formData, mockCategories);
        expect(result.isValid).toBe(false);
        expect(result.errors).toContain('Subcategoria selecionada não existe para esta categoria');
      });
    });
  });

  describe('US-03: Análise e Visualização de Gastos', () => {
    it('should calculate correct metrics for analysis', () => {
      const analysisTransactions: Transaction[] = [
        { id: '1', description: 'Salário', value: 5000, type: 'income', category: 'Salário', date: '2024-01-01' },
        { id: '2', description: 'Bonus', value: 2000, type: 'income', category: 'Salário', date: '2024-01-02' },
        { id: '3', description: 'Alimentação', value: -800, type: 'expense', category: 'Alimentação', date: '2024-01-03' },
        { id: '4', description: 'Transporte', value: -300, type: 'expense', category: 'Transporte', date: '2024-01-04' },
        { id: '5', description: 'Moradia', value: -1500, type: 'expense', category: 'Moradia', date: '2024-01-05' }
      ];

      const metrics = calculateMetrics(analysisTransactions);

      // Verificações para análise
      expect(metrics.income).toBe(7000);
      expect(metrics.expenses).toBe(2600);
      expect(metrics.balance).toBe(4400);

      // Percentuais para visualização
      const incomePercentage = (metrics.income / (metrics.income + metrics.expenses)) * 100;
      const expensePercentage = (metrics.expenses / (metrics.income + metrics.expenses)) * 100;

      expect(Math.round(incomePercentage)).toBe(73); // ~72.92%
      expect(Math.round(expensePercentage)).toBe(27); // ~27.08%
    });
  });

  describe('US-04: Gerenciamento de Metas de Gastos', () => {
    it('should track goal progress correctly', () => {
      const goalTransactions: Transaction[] = [
        { id: '1', description: 'Supermercado 1', value: -200, type: 'expense', category: 'Alimentação', date: '2024-01-01' },
        { id: '2', description: 'Restaurante', value: -150, type: 'expense', category: 'Alimentação', date: '2024-01-02' },
        { id: '3', description: 'Delivery', value: -50, type: 'expense', category: 'Alimentação', date: '2024-01-03' }
      ];

      const alimentacaoGoalLimit = 500;
      const currentExpenses = goalTransactions
        .filter(t => t.category === 'Alimentação' && t.type === 'expense')
        .reduce((sum, t) => sum + Math.abs(t.value), 0);

      expect(currentExpenses).toBe(400); // 200 + 150 + 50

      const progressPercentage = (currentExpenses / alimentacaoGoalLimit) * 100;
      expect(progressPercentage).toBe(80);

      // Status da meta baseado na porcentagem
      let status: 'ok' | 'warning' | 'critical' = 'ok';
      if (progressPercentage >= 100) {
        status = 'critical';
      } else if (progressPercentage >= 80) {
        status = 'warning';
      }

      expect(status).toBe('warning'); // 80% = warning
    });
  });
});