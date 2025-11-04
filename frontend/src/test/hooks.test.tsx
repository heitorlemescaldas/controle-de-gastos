import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTransactions } from '@/hooks/useTransactions';
import { useGoals } from '@/hooks/useGoals';
import { mockCategories, mockTransactions, mockGoals } from '@/data/mockData';
import { TransactionFormData } from '@/types';

describe('Hooks', () => {
  describe('useTransactions', () => {
    it('should initialize with provided transactions', () => {
      const { result } = renderHook(() => 
        useTransactions(mockTransactions, mockCategories)
      );

      expect(result.current.transactions).toHaveLength(mockTransactions.length);
      expect(result.current.metrics.income).toBeGreaterThan(0);
      expect(result.current.metrics.expenses).toBeGreaterThan(0);
    });

    it('should add valid transaction successfully', () => {
      const { result } = renderHook(() => 
        useTransactions([], mockCategories)
      );

      const validFormData: TransactionFormData = {
        description: 'Test Transaction',
        value: '100.50',
        type: 'expense',
        category: 'Alimentação',
        subcategory: 'Supermercado'
      };

      act(() => {
        const response = result.current.addTransaction(validFormData);
        expect(response.success).toBe(true);
        expect(response.errors).toHaveLength(0);
      });

      expect(result.current.transactions).toHaveLength(1);
      expect(result.current.transactions[0].description).toBe('Test Transaction');
      expect(result.current.transactions[0].value).toBe(-100.50);
    });

    it('should reject invalid transaction', () => {
      const { result } = renderHook(() => 
        useTransactions([], mockCategories)
      );

      const invalidFormData: TransactionFormData = {
        description: '', // Invalid: empty description
        value: '0', // Invalid: zero value
        type: 'expense',
        category: '', // Invalid: empty category
        subcategory: ''
      };

      act(() => {
        const response = result.current.addTransaction(invalidFormData);
        expect(response.success).toBe(false);
        expect(response.errors.length).toBeGreaterThan(0);
      });

      expect(result.current.transactions).toHaveLength(0);
    });

    it('should remove transaction successfully', () => {
      const { result } = renderHook(() => 
        useTransactions(mockTransactions, mockCategories)
      );

      const initialLength = result.current.transactions.length;
      const transactionToRemove = result.current.transactions[0];

      act(() => {
        const response = result.current.removeTransaction(transactionToRemove.id);
        expect(response.success).toBe(true);
        expect(response.errors).toHaveLength(0);
      });

      expect(result.current.transactions).toHaveLength(initialLength - 1);
      expect(result.current.transactions.find(t => t.id === transactionToRemove.id)).toBeUndefined();
    });

    it('should reject removal of non-existent transaction', () => {
      const { result } = renderHook(() => 
        useTransactions(mockTransactions, mockCategories)
      );

      act(() => {
        const response = result.current.removeTransaction('non-existent-id');
        expect(response.success).toBe(false);
        expect(response.errors).toContain('Transação não encontrada');
      });
    });

    it('should filter transactions by category', () => {
      const { result } = renderHook(() => 
        useTransactions(mockTransactions, mockCategories)
      );

      const alimentacaoTransactions = result.current.getTransactionsByCategory('Alimentação');
      expect(alimentacaoTransactions.every(t => t.category === 'Alimentação')).toBe(true);
    });

    it('should filter transactions by type', () => {
      const { result } = renderHook(() => 
        useTransactions(mockTransactions, mockCategories)
      );

      const incomeTransactions = result.current.getTransactionsByType('income');
      expect(incomeTransactions.every(t => t.type === 'income')).toBe(true);

      const expenseTransactions = result.current.getTransactionsByType('expense');
      expect(expenseTransactions.every(t => t.type === 'expense')).toBe(true);
    });
  });

  describe('useGoals', () => {
    it('should initialize with provided goals', () => {
      const { result } = renderHook(() => 
        useGoals(mockGoals, mockTransactions)
      );

      expect(result.current.goals).toHaveLength(mockGoals.length);
    });

    it('should update goal status based on transactions', () => {
      const { result } = renderHook(() => 
        useGoals(mockGoals, mockTransactions)
      );

      // Os status devem ser atualizados baseado nos gastos atuais
      const goals = result.current.goals;
      goals.forEach(goal => {
        expect(['ok', 'warning', 'critical']).toContain(goal.status);
      });
    });

    it('should add new goal', () => {
      const { result } = renderHook(() => 
        useGoals([], mockTransactions)
      );

      act(() => {
        const newGoal = result.current.addGoal({
          category: 'Educação',
          limit: 500
        });
        
        expect(newGoal.category).toBe('Educação');
        expect(newGoal.limit).toBe(500);
        expect(newGoal.current).toBe(0);
        expect(newGoal.status).toBe('ok');
      });

      expect(result.current.goals).toHaveLength(1);
    });

    it('should update existing goal', () => {
      const { result } = renderHook(() => 
        useGoals(mockGoals, mockTransactions)
      );

      const goalToUpdate = result.current.goals[0];
      const newLimit = 1000;

      act(() => {
        result.current.updateGoal(goalToUpdate.id, { limit: newLimit });
      });

      const updatedGoal = result.current.goals.find(g => g.id === goalToUpdate.id);
      expect(updatedGoal?.limit).toBe(newLimit);
    });

    it('should remove goal', () => {
      const { result } = renderHook(() => 
        useGoals(mockGoals, mockTransactions)
      );

      const initialLength = result.current.goals.length;
      const goalToRemove = result.current.goals[0];

      act(() => {
        result.current.removeGoal(goalToRemove.id);
      });

      expect(result.current.goals).toHaveLength(initialLength - 1);
      expect(result.current.goals.find(g => g.id === goalToRemove.id)).toBeUndefined();
    });

    it('should get goal by category', () => {
      const { result } = renderHook(() => 
        useGoals(mockGoals, mockTransactions)
      );

      const alimentacaoGoal = result.current.getGoalByCategory('Alimentação');
      expect(alimentacaoGoal?.category).toBe('Alimentação');
    });

    it('should filter goals by status', () => {
      const { result } = renderHook(() => 
        useGoals(mockGoals, mockTransactions)
      );

      const criticalGoals = result.current.getGoalsByStatus('critical');
      expect(criticalGoals.every(g => g.status === 'critical')).toBe(true);
    });
  });
});