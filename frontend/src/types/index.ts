// Tipos de dados do sistema
export interface Transaction {
  id: string;
  description: string;
  value: number;
  type: 'income' | 'expense';
  category: string;
  subcategory?: string;
  date: string;
}

export interface Category {
  id: string;
  name: string;
  subcategories?: string[];
}

export interface Goal {
  id: string;
  category: string;
  limit: number;
  current: number;
  status: 'ok' | 'warning' | 'critical';
}

export interface TransactionFormData {
  description: string;
  value: string;
  type: 'income' | 'expense';
  category: string;
  subcategory: string;
}

// Tipos para validação
export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

export interface ValidationError {
  field: string;
  message: string;
}