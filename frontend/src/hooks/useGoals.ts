import { useState, useMemo } from 'react';
import { Goal, Transaction } from '@/types';
import { updateGoalsStatus } from '@/utils/calculations';

export const useGoals = (initialGoals: Goal[], transactions: Transaction[]) => {
  const [goals, setGoals] = useState<Goal[]>(initialGoals);

  const updatedGoals = useMemo(() => {
    return updateGoalsStatus(goals, transactions);
  }, [goals, transactions]);

  const addGoal = (goal: Omit<Goal, 'id' | 'current' | 'status'>) => {
    const newGoal: Goal = {
      ...goal,
      id: Date.now().toString(),
      current: 0,
      status: 'ok'
    };

    setGoals(prev => [...prev, newGoal]);
    return newGoal;
  };

  const updateGoal = (id: string, updates: Partial<Goal>) => {
    setGoals(prev => prev.map(goal => 
      goal.id === id ? { ...goal, ...updates } : goal
    ));
  };

  const removeGoal = (id: string) => {
    setGoals(prev => prev.filter(goal => goal.id !== id));
  };

  const getGoalByCategory = (category: string) => {
    return updatedGoals.find(goal => goal.category === category);
  };

  const getGoalsByStatus = (status: 'ok' | 'warning' | 'critical') => {
    return updatedGoals.filter(goal => goal.status === status);
  };

  return {
    goals: updatedGoals,
    addGoal,
    updateGoal,
    removeGoal,
    getGoalByCategory,
    getGoalsByStatus
  };
};