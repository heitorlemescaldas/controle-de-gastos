import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import { useTransactions } from "@/hooks/useTransactions";
import { useGoals } from "@/hooks/useGoals";
import { mockCategories, mockTransactions, mockGoals } from "@/data/mockData";
import { TransactionFormData } from "@/types";
import { 
  Wallet, 
  TrendingUp, 
  TrendingDown, 
  Plus, 
  Trash2,
  Target,
  AlertTriangle,
  CheckCircle,
  XCircle,
  DollarSign
} from 'lucide-react';

const Index = () => {
  const { toast } = useToast();
  const { 
    transactions, 
    metrics, 
    addTransaction, 
    removeTransaction 
  } = useTransactions(mockTransactions, mockCategories);
  
  const { goals } = useGoals(mockGoals, transactions);
  
  const [formData, setFormData] = useState<TransactionFormData>({
    description: '',
    value: '',
    type: 'expense',
    category: '',
    subcategory: ''
  });

  // Função para adicionar transação com validações robustas
  const handleAddTransaction = () => {
    const result = addTransaction(formData);
    
    if (!result.success) {
      toast({
        title: "Erro de Validação",
        description: result.errors.join(', '),
        variant: "destructive"
      });
      return;
    }

    // Limpar formulário apenas em caso de sucesso
    setFormData({
      description: '',
      value: '',
      type: 'expense',
      category: '',
      subcategory: ''
    });

    toast({
      title: "Sucesso",
      description: "Transação adicionada com sucesso!",
    });
  };

  // Função para remover transação com validações
  const handleRemoveTransaction = (id: string) => {
    const result = removeTransaction(id);
    
    if (!result.success) {
      toast({
        title: "Erro",
        description: result.errors.join(', '),
        variant: "destructive"
      });
      return;
    }

    toast({
      title: "Transação removida",
      description: "A transação foi removida com sucesso.",
    });
  };

  // Função para obter subcategorias da categoria selecionada
  const getSubcategories = (categoryName: string) => {
    const category = mockCategories.find(c => c.name === categoryName);
    return category?.subcategories || [];
  };

  // Função para obter status da meta
  const getGoalStatusIcon = (status: string) => {
    switch (status) {
      case 'ok': return <CheckCircle className="h-4 w-4 text-success" />;
      case 'warning': return <AlertTriangle className="h-4 w-4 text-warning" />;
      case 'critical': return <XCircle className="h-4 w-4 text-danger" />;
      default: return null;
    }
  };

  return (
    <div className="min-h-screen bg-background p-4 lg:p-8">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <header className="text-center space-y-2">
          <h1 className="text-4xl font-bold text-foreground">Controle de Gastos</h1>
          <p className="text-muted-foreground">Gerencie suas finanças de forma inteligente</p>
        </header>

        {/* Dashboard de Métricas */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="p-6 bg-gradient-card shadow-elevated border-border">
            <div className="flex items-center space-x-4">
              <div className="p-3 bg-gradient-primary rounded-full">
                <Wallet className="h-6 w-6 text-primary-foreground" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Saldo Atual</p>
                <p className={`text-2xl font-bold ${metrics.balance >= 0 ? 'text-success' : 'text-danger'}`}>
                  {new Intl.NumberFormat('pt-BR', { 
                    style: 'currency', 
                    currency: 'BRL' 
                  }).format(metrics.balance)}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6 bg-gradient-card shadow-elevated border-border">
            <div className="flex items-center space-x-4">
              <div className="p-3 bg-gradient-success rounded-full">
                <TrendingUp className="h-6 w-6 text-success-foreground" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Total de Receitas</p>
                <p className="text-2xl font-bold text-success">
                  {new Intl.NumberFormat('pt-BR', { 
                    style: 'currency', 
                    currency: 'BRL' 
                  }).format(metrics.income)}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6 bg-gradient-card shadow-elevated border-border">
            <div className="flex items-center space-x-4">
              <div className="p-3 bg-gradient-danger rounded-full">
                <TrendingDown className="h-6 w-6 text-danger-foreground" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Total de Despesas</p>
                <p className="text-2xl font-bold text-danger">
                  {new Intl.NumberFormat('pt-BR', { 
                    style: 'currency', 
                    currency: 'BRL' 
                  }).format(metrics.expenses)}
                </p>
              </div>
            </div>
          </Card>
        </section>

        {/* Layout Principal */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Coluna da Esquerda */}
          <div className="space-y-8">
            
            {/* Formulário de Transação */}
            <Card className="p-6 bg-gradient-card shadow-card border-border">
              <div className="flex items-center space-x-2 mb-6">
                <Plus className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-semibold text-foreground">Nova Transação</h2>
              </div>
              
              <div className="space-y-4">
                <div>
                  <Label htmlFor="description" className="text-foreground">Descrição</Label>
                  <Input
                    id="description"
                    value={formData.description}
                    onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                    placeholder="Ex: Compra no supermercado"
                    className="mt-1 bg-background border-border text-foreground"
                  />
                </div>

                <div>
                  <Label htmlFor="value" className="text-foreground">Valor</Label>
                  <Input
                    id="value"
                    type="number"
                    value={formData.value}
                    onChange={(e) => setFormData(prev => ({ ...prev, value: e.target.value }))}
                    placeholder="0,00"
                    className="mt-1 bg-background border-border text-foreground"
                  />
                </div>

                <div>
                  <Label htmlFor="type" className="text-foreground">Tipo</Label>
                  <Select value={formData.type} onValueChange={(value: 'income' | 'expense') => 
                    setFormData(prev => ({ ...prev, type: value }))}>
                    <SelectTrigger className="mt-1 bg-background border-border text-foreground">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-popover border-border">
                      <SelectItem value="expense" className="text-foreground">Despesa</SelectItem>
                      <SelectItem value="income" className="text-foreground">Receita</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="category" className="text-foreground">Categoria</Label>
                  <Select value={formData.category} onValueChange={(value) => 
                    setFormData(prev => ({ ...prev, category: value, subcategory: '' }))}>
                    <SelectTrigger className="mt-1 bg-background border-border text-foreground">
                      <SelectValue placeholder="Selecione uma categoria" />
                    </SelectTrigger>
                    <SelectContent className="bg-popover border-border">
                      {mockCategories.map(category => (
                        <SelectItem key={category.id} value={category.name} className="text-foreground">
                          {category.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {formData.category && getSubcategories(formData.category).length > 0 && (
                  <div>
                    <Label htmlFor="subcategory" className="text-foreground">Subcategoria</Label>
                    <Select value={formData.subcategory} onValueChange={(value) => 
                      setFormData(prev => ({ ...prev, subcategory: value }))}>
                      <SelectTrigger className="mt-1 bg-background border-border text-foreground">
                        <SelectValue placeholder="Selecione uma subcategoria" />
                      </SelectTrigger>
                      <SelectContent className="bg-popover border-border">
                        {getSubcategories(formData.category).map(subcategory => (
                          <SelectItem key={subcategory} value={subcategory} className="text-foreground">
                            {subcategory}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                )}

                <Button 
                  onClick={handleAddTransaction} 
                  className="w-full bg-gradient-primary hover:opacity-90 transition-opacity text-primary-foreground"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Adicionar Transação
                </Button>
              </div>
            </Card>

            {/* Metas de Gastos */}
            <Card className="p-6 bg-gradient-card shadow-card border-border">
              <div className="flex items-center space-x-2 mb-6">
                <Target className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-semibold text-foreground">Metas de Gastos</h2>
              </div>
              
              <div className="space-y-4">
                {goals.map(goal => (
                  <div key={goal.id} className="flex items-center justify-between p-4 bg-background rounded-lg border border-border">
                    <div className="flex items-center space-x-3">
                      {getGoalStatusIcon(goal.status)}
                      <div>
                        <p className="font-medium text-foreground">{goal.category}</p>
                        <p className="text-sm text-muted-foreground">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(goal.current)} / {' '}
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(goal.limit)}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-foreground">
                        {Math.round((goal.current / goal.limit) * 100)}%
                      </p>
                      <div className="w-20 h-2 bg-muted rounded-full mt-1">
                        <div 
                          className={`h-full rounded-full ${
                            goal.status === 'ok' ? 'bg-success' : 
                            goal.status === 'warning' ? 'bg-warning' : 'bg-danger'
                          }`}
                          style={{ width: `${Math.min((goal.current / goal.limit) * 100, 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>

          {/* Coluna da Direita */}
          <div>
            {/* Lista de Transações */}
            <Card className="p-6 bg-gradient-card shadow-card border-border">
              <div className="flex items-center space-x-2 mb-6">
                <DollarSign className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-semibold text-foreground">Transações Recentes</h2>
              </div>
              
              <div className="space-y-3 max-h-96 overflow-y-auto">
                {transactions.map(transaction => (
                  <div key={transaction.id} className="flex items-center justify-between p-4 bg-background rounded-lg border border-border hover:shadow-card transition-shadow">
                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <p className="font-medium text-foreground">{transaction.description}</p>
                        <p className={`font-semibold ${
                          transaction.type === 'income' ? 'text-success' : 'text-danger'
                        }`}>
                          {transaction.type === 'income' ? '+' : ''}{new Intl.NumberFormat('pt-BR', { 
                            style: 'currency', 
                            currency: 'BRL' 
                          }).format(transaction.value)}
                        </p>
                      </div>
                      <div className="flex items-center justify-between mt-1">
                        <p className="text-sm text-muted-foreground">
                          {transaction.category}{transaction.subcategory ? ` • ${transaction.subcategory}` : ''}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(transaction.date).toLocaleDateString('pt-BR')}
                        </p>
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveTransaction(transaction.id)}
                      className="ml-4 text-muted-foreground hover:text-danger hover:bg-danger/10"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
                
                {transactions.length === 0 && (
                  <div className="text-center py-8">
                    <p className="text-muted-foreground">Nenhuma transação encontrada</p>
                  </div>
                )}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Index;