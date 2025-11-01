import { Transaction, Goal } from '@/types';

/**
 * Calcula métricas financeiras a partir das transações
 * US-01 / C05: Registrar despesa e verificar o estado do agregado
 */
export const calculateMetrics = (transactions: Transaction[]) => {
  const income = transactions
    .filter(t => t.type === 'income')
    .reduce((sum, t) => sum + t.value, 0);
  
  const expenses = transactions
    .filter(t => t.type === 'expense')
    .reduce((sum, t) => sum + Math.abs(t.value), 0);
  
  const balance = income - expenses;
  
  return { income, expenses, balance };
};

/**
 * Calcula o gasto atual por categoria
 */
export const calculateCategoryExpenses = (transactions: Transaction[], category: string): number => {
  return transactions
    .filter(t => t.type === 'expense' && t.category === category)
    .reduce((sum, t) => sum + Math.abs(t.value), 0);
};

/**
 * Atualiza o status das metas baseado nos gastos atuais
 * US-04: Gerenciamento de Metas de Gastos
 */
export const updateGoalsStatus = (goals: Goal[], transactions: Transaction[]): Goal[] => {
  return goals.map(goal => {
    const currentExpense = calculateCategoryExpenses(transactions, goal.category);
    const percentage = (currentExpense / goal.limit) * 100;
    
    let status: 'ok' | 'warning' | 'critical' = 'ok';
    
    if (percentage >= 100) {
      status = 'critical';
    } else if (percentage >= 80) {
      status = 'warning';
    }
    
    return {
      ...goal,
      current: currentExpense,
      status
    };
  });
};

/**
 * Valida se o estado dos agregados está correto após operação
 */
export const validateAggregateState = (
  transactions: Transaction[],
  expectedIncome: number,
  expectedExpenses: number
): boolean => {
  const metrics = calculateMetrics(transactions);
  
  return (
    Math.abs(metrics.income - expectedIncome) < 0.01 &&
    Math.abs(metrics.expenses - expectedExpenses) < 0.01
  );
};