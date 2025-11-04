import React, { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useToast } from "@/components/ui/use-toast";

import { Wallet, TrendingUp, TrendingDown, Plus, DollarSign } from "lucide-react";

import { listCategories, type CategoryNode } from "@/services/categories";
import { createExpense, toIsoTimestamp, type CreateExpenseRequest } from "@/services/expenses";
import { usePeriodReport } from "@/hooks/useReports";

// ===== helpers de período (mês atual) =====
function getMonthRange(): { start: string; end: string } {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().slice(0, 10); // YYYY-MM-DD
  const end = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().slice(0, 10);
  return { start, end };
}

type FormState = {
  description: string;
  amount: string; // manter string para enviar ao back (Swagger define string)
  type: "expense" | "income";
  categoryId: string;
};

const Index = () => {
  const { toast } = useToast();

  // ===== categorias reais do backend =====
  const [categories, setCategories] = useState<CategoryNode[]>([]);
  const [loadingCats, setLoadingCats] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        setLoadingCats(true);
        const data = await listCategories();
        setCategories(data ?? []);
      } catch (e: any) {
        toast({ title: "Falha ao carregar categorias", description: e?.message ?? "Erro", variant: "destructive" });
      } finally {
        setLoadingCats(false);
      }
    })();
  }, [toast]);

  // ===== relatório por período (cards do dashboard) =====
  const { start, end } = useMemo(getMonthRange, []);
  const { data: report, isPending: loadingReport, refetch: refetchReport, error: reportError } = usePeriodReport({
    start,
    end,
  });

  // ===== formulário de nova transação (DESPESA/RECEITA) =====
  const [form, setForm] = useState<FormState>({
    description: "",
    amount: "",
    type: "expense",
    categoryId: "",
  });

  async function handleAddTransaction() {
    // validações simples no front (o back também valida)
    const errors: string[] = [];
    if (!form.description.trim()) errors.push("Descrição é obrigatória");
    if (!form.amount || Number(form.amount) <= 0) errors.push("Valor deve ser maior que zero");
    if (!form.categoryId) errors.push("Selecione uma categoria");
    if (errors.length) {
      toast({ title: "Erro de validação", description: errors.join(", "), variant: "destructive" });
      return;
    }

    const payload: CreateExpenseRequest = {
      amount: form.amount.replace(",", "."), // normaliza decimal para ponto
      type: form.type === "expense" ? "DEBIT" : "CREDIT",
      description: form.description.trim(),
      timestamp: toIsoTimestamp(new Date()), // agora
      categoryId: form.categoryId,
    };

    try {
      await createExpense(payload);
      toast({ title: "Transação criada com sucesso" });
      setForm({ description: "", amount: "", type: "expense", categoryId: "" });
      // atualiza cards/itens do relatório
      await refetchReport();
    } catch (e: any) {
      toast({ title: "Falha ao criar transação", description: e?.message ?? "Erro", variant: "destructive" });
    }
  }

  // ===== métricas =====
  const balance = report?.balance ?? 0;
  const totalIncome = report?.totalCredit ?? 0;
  const totalExpenses = report?.totalDebit ?? 0;

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
                <p className="text-sm text-muted-foreground">Saldo ({start} a {end})</p>
                <p className={`text-2xl font-bold ${balance >= 0 ? "text-success" : "text-danger"}`}>
                  {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(balance)}
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
                  {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(totalIncome)}
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
                  {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(totalExpenses)}
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
                    value={form.description}
                    onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                    placeholder="Ex: Compra no supermercado"
                    className="mt-1 bg-background border-border text-foreground"
                  />
                </div>

                <div>
                  <Label htmlFor="amount" className="text-foreground">Valor</Label>
                  <Input
                    id="amount"
                    type="number"
                    value={form.amount}
                    onChange={(e) => setForm((p) => ({ ...p, amount: e.target.value }))}
                    placeholder="0.00"
                    className="mt-1 bg-background border-border text-foreground"
                    step="0.01"
                    min="0"
                  />
                </div>

                <div>
                  <Label htmlFor="type" className="text-foreground">Tipo</Label>
                  <Select value={form.type} onValueChange={(value: "income" | "expense") => setForm((p) => ({ ...p, type: value }))}>
                    <SelectTrigger className="mt-1 bg-background border-border text-foreground">
                      <SelectValue placeholder="Selecione o tipo" />
                    </SelectTrigger>
                    <SelectContent className="bg-popover border-border">
                      <SelectItem value="expense" className="text-foreground">Despesa (DEBIT)</SelectItem>
                      <SelectItem value="income" className="text-foreground">Receita (CREDIT)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="category" className="text-foreground">Categoria</Label>
                  <Select
                    value={form.categoryId}
                    onValueChange={(value) => setForm((p) => ({ ...p, categoryId: value }))}
                    disabled={loadingCats}
                  >
                    <SelectTrigger className="mt-1 bg-background border-border text-foreground">
                      <SelectValue placeholder={loadingCats ? "Carregando..." : "Selecione uma categoria"} />
                    </SelectTrigger>
                    <SelectContent className="bg-popover border-border">
                      {categories.map((cat) => (
                        <SelectItem key={cat.id} value={cat.id} className="text-foreground">
                          {cat.path || cat.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <Button onClick={handleAddTransaction} className="w-full bg-gradient-primary hover:opacity-90 transition-opacity text-primary-foreground">
                  <Plus className="h-4 w-4 mr-2" />
                  Adicionar Transação
                </Button>
              </div>
            </Card>
          </div>

          {/* Coluna da Direita: Itens do relatório (período atual) */}
          <div>
            <Card className="p-6 bg-gradient-card shadow-card border-border">
              <div className="flex items-center space-x-2 mb-6">
                <DollarSign className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-semibold text-foreground">Gastos por Categoria (Período)</h2>
              </div>

              {loadingReport && <div className="text-sm text-muted-foreground">Carregando relatório…</div>}
              {reportError && <div className="text-sm text-red-600">Erro ao carregar relatório</div>}

              <div className="space-y-3 max-h-96 overflow-y-auto">
                {report?.items?.map((it, idx) => (
                  <div key={idx} className="flex items-center justify-between p-4 bg-background rounded-lg border border-border">
                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <p className="font-medium text-foreground">{it.categoryPath}</p>
                        <p className="font-semibold text-danger">
                          {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(it.debit)}
                        </p>
                      </div>
                      <div className="flex items-center justify-between mt-1">
                        <p className="text-sm text-muted-foreground">Créditos</p>
                        <p className="text-sm text-success">
                          {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(it.credit)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}

                {(!report || report.items?.length === 0) && !loadingReport && (
                  <div className="text-center py-8">
                    <p className="text-muted-foreground">Nenhum item no período selecionado</p>
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