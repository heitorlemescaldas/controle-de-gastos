import { describe, it, expect } from 'vitest';
import { 
  calculateMetrics, 
  calculateCategoryExpenses,
  updateGoalsStatus,
  validateAggregateState
} from '@/utils/calculations';
import { Transaction, Goal } from '@/types';

describe('Calculations Utils', () => {
  const mockTransactions: Transaction[] = [
    {
      id: '1',
      description: 'Salário',
      value: 5000,
      type: 'income',
      category: 'Salário',
      date: '2024-01-01'
    },
    {
      id: '2',
      description: 'Supermercado',
      value: -200,
      type: 'expense',
      category: 'Alimentação',
      date: '2024-01-02'
    },
    {
      id: '3',
      description: 'Restaurante',
      value: -80,
      type: 'expense',
      category: 'Alimentação',
      date: '2024-01-03'
    },
    {
      id: '4',
      description: 'Combustível',
      value: -150,
      type: 'expense',
      category: 'Transporte',
      date: '2024-01-04'
    }
  ];

  describe('calculateMetrics', () => {
    // US-01 / C05: Registrar despesa e verificar o estado do agregado
    it('should calculate correct income, expenses and balance', () => {
      const metrics = calculateMetrics(mockTransactions);
      
      expect(metrics.income).toBe(5000);
      expect(metrics.expenses).toBe(430); // 200 + 80 + 150
      expect(metrics.balance).toBe(4570); // 5000 - 430
    });

    it('should handle empty transactions array', () => {
      const metrics = calculateMetrics([]);
      
      expect(metrics.income).toBe(0);
      expect(metrics.expenses).toBe(0);
      expect(metrics.balance).toBe(0);
    });

    it('should handle only income transactions', () => {
      const incomeOnly = mockTransactions.filter(t => t.type === 'income');
      const metrics = calculateMetrics(incomeOnly);
      
      expect(metrics.income).toBe(5000);
      expect(metrics.expenses).toBe(0);
      expect(metrics.balance).toBe(5000);
    });

    it('should handle only expense transactions', () => {
      const expenseOnly = mockTransactions.filter(t => t.type === 'expense');
      const metrics = calculateMetrics(expenseOnly);
      
      expect(metrics.income).toBe(0);
      expect(metrics.expenses).toBe(430);
      expect(metrics.balance).toBe(-430);
    });
  });

  describe('calculateCategoryExpenses', () => {
    it('should calculate expenses for specific category', () => {
      const alimentacaoExpenses = calculateCategoryExpenses(mockTransactions, 'Alimentação');
      expect(alimentacaoExpenses).toBe(280); // 200 + 80

      const transporteExpenses = calculateCategoryExpenses(mockTransactions, 'Transporte');
      expect(transporteExpenses).toBe(150);
    });

    it('should return 0 for category with no expenses', () => {
      const expenses = calculateCategoryExpenses(mockTransactions, 'Lazer');
      expect(expenses).toBe(0);
    });

    it('should not include income in category expenses', () => {
      const salarioExpenses = calculateCategoryExpenses(mockTransactions, 'Salário');
      expect(salarioExpenses).toBe(0); // Salário é receita, não despesa
    });
  });

  describe('updateGoalsStatus', () => {
    const mockGoals: Goal[] = [
      { id: '1', category: 'Alimentação', limit: 300, current: 0, status: 'ok' },
      { id: '2', category: 'Transporte', limit: 100, current: 0, status: 'ok' },
      { id: '3', category: 'Lazer', limit: 200, current: 0, status: 'ok' }
    ];

    // US-04: Gerenciamento de Metas de Gastos
    it('should update goals status based on current expenses', () => {
      const updatedGoals = updateGoalsStatus(mockGoals, mockTransactions);
      
      // Alimentação: 280/300 = 93.33% -> warning (>= 80%)
      const alimentacaoGoal = updatedGoals.find(g => g.category === 'Alimentação');
      expect(alimentacaoGoal?.current).toBe(280);
      expect(alimentacaoGoal?.status).toBe('warning');

      // Transporte: 150/100 = 150% -> critical (>= 100%)
      const transporteGoal = updatedGoals.find(g => g.category === 'Transporte');
      expect(transporteGoal?.current).toBe(150);
      expect(transporteGoal?.status).toBe('critical');

      // Lazer: 0/200 = 0% -> ok (< 80%)
      const lazerGoal = updatedGoals.find(g => g.category === 'Lazer');
      expect(lazerGoal?.current).toBe(0);
      expect(lazerGoal?.status).toBe('ok');
    });
  });

  describe('validateAggregateState', () => {
    it('should validate correct aggregate state', () => {
      const isValid = validateAggregateState(mockTransactions, 5000, 430);
      expect(isValid).toBe(true);
    });

    it('should reject incorrect aggregate state', () => {
      const isValid = validateAggregateState(mockTransactions, 4000, 300);
      expect(isValid).toBe(false);
    });

    it('should handle floating point precision', () => {
      // Teste com números que podem ter problemas de precisão
      const transactions: Transaction[] = [
        { id: '1', description: 'Test', value: 100.1, type: 'income', category: 'Test', date: '2024-01-01' },
        { id: '2', description: 'Test', value: -50.05, type: 'expense', category: 'Test', date: '2024-01-01' }
      ];
      
      const isValid = validateAggregateState(transactions, 100.1, 50.05);
      expect(isValid).toBe(true);
    });
  });
});