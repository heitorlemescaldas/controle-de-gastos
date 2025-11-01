import { useState, useMemo } from 'react';
import { Transaction, TransactionFormData, Category } from '@/types';
import { validateTransactionForm, validateTransactionRemoval } from '@/utils/validation';
import { calculateMetrics } from '@/utils/calculations';

export const useTransactions = (initialTransactions: Transaction[], categories: Category[]) => {
  const [transactions, setTransactions] = useState<Transaction[]>(initialTransactions);

  const metrics = useMemo(() => calculateMetrics(transactions), [transactions]);

  const addTransaction = (formData: TransactionFormData) => {
    // Validação completa do formulário
    const validation = validateTransactionForm(formData, categories);
    
    if (!validation.isValid) {
      return {
        success: false,
        errors: validation.errors
      };
    }

    const value = parseFloat(formData.value);
    const newTransaction: Transaction = {
      id: Date.now().toString(),
      description: formData.description.trim(),
      value: formData.type === 'expense' ? -value : value,
      type: formData.type,
      category: formData.category,
      subcategory: formData.subcategory || undefined,
      date: new Date().toISOString().split('T')[0]
    };

    setTransactions(prev => [newTransaction, ...prev]);
    
    return {
      success: true,
      transaction: newTransaction,
      errors: []
    };
  };

  const removeTransaction = (id: string) => {
    // Validação da remoção
    const validation = validateTransactionRemoval(id);
    
    if (!validation.isValid) {
      return {
        success: false,
        errors: validation.errors
      };
    }

    const transactionExists = transactions.some(t => t.id === id);
    
    if (!transactionExists) {
      return {
        success: false,
        errors: ['Transação não encontrada']
      };
    }

    setTransactions(prev => prev.filter(t => t.id !== id));
    
    return {
      success: true,
      errors: []
    };
  };

  const getTransactionsByCategory = (category: string) => {
    return transactions.filter(t => t.category === category);
  };

  const getTransactionsByType = (type: 'income' | 'expense') => {
    return transactions.filter(t => t.type === type);
  };

  return {
    transactions,
    metrics,
    addTransaction,
    removeTransaction,
    getTransactionsByCategory,
    getTransactionsByType
  };
};