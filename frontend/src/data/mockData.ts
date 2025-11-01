import { Transaction, Category, Goal } from '@/types';

export const mockCategories: Category[] = [
  { id: '1', name: 'Alimentação', subcategories: ['Supermercado', 'Restaurantes', 'Delivery'] },
  { id: '2', name: 'Transporte', subcategories: ['Combustível', 'Transporte Público', 'Uber/Taxi'] },
  { id: '3', name: 'Moradia', subcategories: ['Aluguel', 'Condomínio', 'Energia', 'Água'] },
  { id: '4', name: 'Lazer', subcategories: ['Cinema', 'Viagens', 'Hobbies'] },
  { id: '5', name: 'Salário', subcategories: ['Salário Principal', 'Freelances', 'Bonificações'] },
  { id: '6', name: 'Investimentos', subcategories: ['Dividendos', 'Vendas', 'Lucros'] },
];

export const mockTransactions: Transaction[] = [
  {
    id: '1',
    description: 'Salário Janeiro',
    value: 5000,
    type: 'income',
    category: 'Salário',
    subcategory: 'Salário Principal',
    date: '2024-01-01'
  },
  {
    id: '2',
    description: 'Supermercado Extra',
    value: -320.50,
    type: 'expense',
    category: 'Alimentação',
    subcategory: 'Supermercado',
    date: '2024-01-02'
  },
  {
    id: '3',
    description: 'Combustível',
    value: -180.00,
    type: 'expense',
    category: 'Transporte',
    subcategory: 'Combustível',
    date: '2024-01-03'
  },
  {
    id: '4',
    description: 'Freelance',
    value: 1200,
    type: 'income',
    category: 'Salário',
    subcategory: 'Freelances',
    date: '2024-01-04'
  },
  {
    id: '5',
    description: 'Aluguel',
    value: -1500,
    type: 'expense',
    category: 'Moradia',
    subcategory: 'Aluguel',
    date: '2024-01-05'
  }
];

export const mockGoals: Goal[] = [
  { id: '1', category: 'Alimentação', limit: 800, current: 320.50, status: 'ok' },
  { id: '2', category: 'Transporte', limit: 400, current: 380, status: 'warning' },
  { id: '3', category: 'Moradia', limit: 1600, current: 1500, status: 'ok' },
  { id: '4', category: 'Lazer', limit: 300, current: 280, status: 'critical' },
];