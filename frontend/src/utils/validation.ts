import { TransactionFormData, ValidationResult, Category } from '@/types';

/**
 * Valida se uma descrição é válida
 * US-01 / C03: Tentar registrar uma despesa com descrição vazia ou nula
 */
export const validateDescription = (description: string): ValidationResult => {
  const errors: string[] = [];
  
  if (!description || description.trim() === '') {
    errors.push('Descrição é obrigatória');
  }
  
  if (description && description.trim().length < 3) {
    errors.push('Descrição deve ter pelo menos 3 caracteres');
  }
  
  if (description && description.trim().length > 100) {
    errors.push('Descrição deve ter no máximo 100 caracteres');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Valida se um valor é válido
 */
export const validateValue = (value: string): ValidationResult => {
  const errors: string[] = [];
  
  if (!value || value.trim() === '') {
    errors.push('Valor é obrigatório');
  }
  
  const numericValue = parseFloat(value);
  
  if (isNaN(numericValue)) {
    errors.push('Valor deve ser um número válido');
  } else if (numericValue <= 0) {
    errors.push('Valor deve ser maior que zero');
  } else if (numericValue > 999999999) {
    errors.push('Valor não pode exceder R$ 999.999.999,00');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Valida se uma categoria é válida
 * US-01 / C04: Tentar registrar uma despesa com categoria inexistente
 */
export const validateCategory = (category: string, availableCategories: Category[]): ValidationResult => {
  const errors: string[] = [];
  
  if (!category || category.trim() === '') {
    errors.push('Categoria é obrigatória');
  }
  
  const categoryExists = availableCategories.some(cat => cat.name === category);
  if (category && !categoryExists) {
    errors.push('Categoria selecionada não existe');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Valida se uma subcategoria é válida para a categoria selecionada
 */
export const validateSubcategory = (
  subcategory: string, 
  category: string, 
  availableCategories: Category[]
): ValidationResult => {
  const errors: string[] = [];
  
  if (!subcategory) {
    return { isValid: true, errors: [] }; // Subcategoria é opcional
  }
  
  const selectedCategory = availableCategories.find(cat => cat.name === category);
  
  if (selectedCategory && selectedCategory.subcategories) {
    const subcategoryExists = selectedCategory.subcategories.includes(subcategory);
    if (!subcategoryExists) {
      errors.push('Subcategoria selecionada não existe para esta categoria');
    }
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Valida todo o formulário de transação
 */
export const validateTransactionForm = (
  formData: TransactionFormData, 
  availableCategories: Category[]
): ValidationResult => {
  const allErrors: string[] = [];
  
  const descriptionValidation = validateDescription(formData.description);
  const valueValidation = validateValue(formData.value);
  const categoryValidation = validateCategory(formData.category, availableCategories);
  const subcategoryValidation = validateSubcategory(formData.subcategory, formData.category, availableCategories);
  
  allErrors.push(...descriptionValidation.errors);
  allErrors.push(...valueValidation.errors);
  allErrors.push(...categoryValidation.errors);
  allErrors.push(...subcategoryValidation.errors);
  
  return {
    isValid: allErrors.length === 0,
    errors: allErrors
  };
};

/**
 * Valida se uma transação pode ser removida
 */
export const validateTransactionRemoval = (transactionId: string): ValidationResult => {
  const errors: string[] = [];
  
  if (!transactionId || transactionId.trim() === '') {
    errors.push('ID da transação é obrigatório para remoção');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};