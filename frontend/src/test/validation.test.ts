import { describe, it, expect } from 'vitest';
import { 
  validateDescription, 
  validateValue, 
  validateCategory,
  validateSubcategory,
  validateTransactionForm,
  validateTransactionRemoval
} from '@/utils/validation';
import { mockCategories } from '@/data/mockData';
import { TransactionFormData } from '@/types';

describe('Validation Utils', () => {
  describe('validateDescription', () => {
    // US-01 / C03: Tentar registrar uma despesa com descrição vazia ou nula
    it('should reject empty description', () => {
      const result = validateDescription('');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Descrição é obrigatória');
    });

    it('should reject null/undefined description', () => {
      const result = validateDescription(null as any);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Descrição é obrigatória');
    });

    it('should reject whitespace-only description', () => {
      const result = validateDescription('   ');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Descrição é obrigatória');
    });

    it('should reject description with less than 3 characters', () => {
      const result = validateDescription('ab');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Descrição deve ter pelo menos 3 caracteres');
    });

    it('should reject description with more than 100 characters', () => {
      const longDescription = 'a'.repeat(101);
      const result = validateDescription(longDescription);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Descrição deve ter no máximo 100 caracteres');
    });

    it('should accept valid description', () => {
      const result = validateDescription('Compra no supermercado');
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });
  });

  describe('validateValue', () => {
    it('should reject empty value', () => {
      const result = validateValue('');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Valor é obrigatório');
    });

    it('should reject non-numeric value', () => {
      const result = validateValue('abc');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Valor deve ser um número válido');
    });

    it('should reject zero value', () => {
      const result = validateValue('0');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Valor deve ser maior que zero');
    });

    it('should reject negative value', () => {
      const result = validateValue('-100');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Valor deve ser maior que zero');
    });

    it('should reject extremely large value', () => {
      const result = validateValue('9999999999');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Valor não pode exceder R$ 999.999.999,00');
    });

    it('should accept valid positive value', () => {
      const result = validateValue('100.50');
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });
  });

  describe('validateCategory', () => {
    // US-01 / C04: Tentar registrar uma despesa com categoria inexistente
    it('should reject empty category', () => {
      const result = validateCategory('', mockCategories);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Categoria é obrigatória');
    });

    it('should reject non-existent category', () => {
      const result = validateCategory('Categoria Inexistente', mockCategories);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Categoria selecionada não existe');
    });

    it('should accept existing category', () => {
      const result = validateCategory('Alimentação', mockCategories);
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });
  });

  describe('validateSubcategory', () => {
    it('should accept empty subcategory (optional field)', () => {
      const result = validateSubcategory('', 'Alimentação', mockCategories);
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('should reject non-existent subcategory', () => {
      const result = validateSubcategory('Subcategoria Inexistente', 'Alimentação', mockCategories);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Subcategoria selecionada não existe para esta categoria');
    });

    it('should accept existing subcategory', () => {
      const result = validateSubcategory('Supermercado', 'Alimentação', mockCategories);
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });
  });

  describe('validateTransactionForm', () => {
    const validFormData: TransactionFormData = {
      description: 'Compra no supermercado',
      value: '100.50',
      type: 'expense',
      category: 'Alimentação',
      subcategory: 'Supermercado'
    };

    it('should validate complete valid form', () => {
      const result = validateTransactionForm(validFormData, mockCategories);
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('should collect all validation errors', () => {
      const invalidFormData: TransactionFormData = {
        description: '',
        value: '0',
        type: 'expense',
        category: '',
        subcategory: ''
      };

      const result = validateTransactionForm(invalidFormData, mockCategories);
      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBeGreaterThan(0);
      expect(result.errors).toContain('Descrição é obrigatória');
      expect(result.errors).toContain('Valor deve ser maior que zero');
      expect(result.errors).toContain('Categoria é obrigatória');
    });
  });

  describe('validateTransactionRemoval', () => {
    it('should reject empty transaction ID', () => {
      const result = validateTransactionRemoval('');
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('ID da transação é obrigatório para remoção');
    });

    it('should accept valid transaction ID', () => {
      const result = validateTransactionRemoval('123');
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });
  });
});