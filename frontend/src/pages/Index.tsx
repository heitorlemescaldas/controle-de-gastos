import React, { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useToast } from "@/components/ui/use-toast";
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  Plus,
  DollarSign,
  FolderPlus,
  Pencil,
  Trash2,
  CornerDownRight,
  MoveRight,
} from "lucide-react";

import {
  type CategoryNode,
  listCategories,
  createRootCategory,
  createChildCategory,
  renameCategory,
  deleteCategory,
  moveCategory,
} from "@/services/categories";
import { createExpense, toIsoTimestamp, type CreateExpenseRequest } from "@/services/expenses";
import { usePeriodReport } from "@/hooks/useReports";
import { useGoalEvaluation, useSetGoal } from "@/hooks/useGoalsApi";
import { clearToken } from "@/stores/session";

/** ---------- helpers de datas ---------- */
function monthStartEndUtcISO(year: number, monthIndex0: number) {
  const start = new Date(Date.UTC(year, monthIndex0, 1, 0, 0, 0, 0));
  const end = new Date(Date.UTC(year, monthIndex0 + 1, 0, 23, 59, 59, 999));
  return { start: start.toISOString(), end: end.toISOString() };
}
function yyyyMM(d = new Date()) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}
function firstDayOf(ym: string) {
  const [y, m] = ym.split("-").map(Number);
  const yyyy = String(y).padStart(4, "0");
  const mm = String(m).padStart(2, "0");
  return `${yyyy}-${mm}-01`;
}
function decodeEmail(): string | null {
  try {
    const token = localStorage.getItem("auth_token");
    if (!token) return null;
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return payload?.sub ?? null;
  } catch {
    return null;
  }
}

/** ---------- página ---------- */
type FormState = {
  description: string;
  amount: string;
  type: "expense" | "income";
  categoryId: string;
  date: string; // YYYY-MM-DD
};

const Index = () => {
  const { toast } = useToast();
  const userEmail = decodeEmail();

  /** ---------------- período (mês atual por padrão) ---------------- */
  const [month, setMonth] = useState<string>(yyyyMM());
  const { startISO, endISO } = useMemo(() => {
    const [y, m] = month.split("-").map(Number);
    const { start, end } = monthStartEndUtcISO(y, (m ?? 1) - 1);
    return { startISO: start, endISO: end };
  }, [month]);

  /** ---------------- categorias ---------------- */
  const [categories, setCategories] = useState<CategoryNode[]>([]);
  const [loadingCats, setLoadingCats] = useState(false);
  const [selectedCatId, setSelectedCatId] = useState<string>(""); // para ações de rename/delete/move

  async function loadCategories() {
    try {
      setLoadingCats(true);
      const data = await listCategories();
      setCategories(data ?? []);
      if (data?.length && !selectedCatId) setSelectedCatId(data[0].id);
    } catch (e: any) {
      toast({
        title: "Falha ao carregar categorias",
        description: e?.message ?? "Erro",
        variant: "destructive",
      });
    } finally {
      setLoadingCats(false);
    }
  }

  useEffect(() => {
    loadCategories();
  }, []);

  /** ---------------- relatório ---------------- */
  const {
    data: report,
    isPending: loadingReport,
    refetch: refetchReport,
    error: reportError,
  } = usePeriodReport({ start: startISO, end: endISO });

  /** ---------------- criar transação ---------------- */
  const [form, setForm] = useState<FormState>({
    description: "",
    amount: "",
    type: "expense",
    categoryId: "",
    date: firstDayOf(yyyyMM()), // default: 1º dia do mês atual
  });

  // sempre que o usuário mudar o mês do dashboard, sincronizamos a data da transação
  useEffect(() => {
    setForm((f) => ({ ...f, date: firstDayOf(month) }));
  }, [month]);

  async function handleAddTransaction() {
    const errors: string[] = [];
    if (!form.description.trim()) errors.push("Descrição é obrigatória");
    if (!form.amount || Number(form.amount) <= 0) errors.push("Valor deve ser maior que zero");
    if (!form.categoryId) errors.push("Selecione uma categoria");
    if (!/^\d{4}-\d{2}-\d{2}$/.test(form.date)) errors.push("Data inválida (use YYYY-MM-DD)");
    if (errors.length) {
      toast({ title: "Erro de validação", description: errors.join(", "), variant: "destructive" });
      return;
    }

    const payload: CreateExpenseRequest = {
      amount: form.amount.replace(",", "."),
      type: form.type === "expense" ? "DEBIT" : "CREDIT",
      description: form.description.trim(),
      // envia no padrão aceito pelo back: "YYYY-MM-DDT00:00:00"
      timestamp: toIsoTimestamp(form.date),
      categoryId: form.categoryId,
    };

    try {
      await createExpense(payload);
      toast({ title: "Transação criada com sucesso" });
      setForm({ description: "", amount: "", type: "expense", categoryId: "", date: firstDayOf(month) });
      // atualiza cards/relatório e metas (caso o usuário esteja avaliando este mês/categoria)
      await refetchReport();
      await refetchGoal();
    } catch (e: any) {
      toast({ title: "Falha ao criar transação", description: e?.message ?? "Erro", variant: "destructive" });
    }
  }

  /** ---------------- metas ---------------- */
  const [goalRootId, setGoalRootId] = useState<string>("");
  const [goalMonth, setGoalMonth] = useState<string>(yyyyMM());
  const [goalLimit, setGoalLimit] = useState<string>("0.00");

  const { data: goalEval, isPending: loadingGoal, refetch: refetchGoal } = useGoalEvaluation({
    rootCategoryId: goalRootId,
    month: goalMonth,
  });
  const setGoalMutation = useSetGoal();

  const handleSetGoal = async () => {
    if (!goalRootId || !goalMonth || Number(goalLimit) <= 0) {
      toast({ title: "Preencha categoria, mês e limite > 0", variant: "destructive" });
      return;
    }
    try {
      await setGoalMutation.mutateAsync({
        rootCategoryId: goalRootId,
        month: goalMonth,
        limit: goalLimit.replace(",", "."),
      });
      toast({ title: "Meta definida/atualizada com sucesso" });
      await refetchGoal();
    } catch (e: any) {
      toast({ title: "Falha ao definir meta", description: e?.message ?? "Erro", variant: "destructive" });
    }
  };

  // reavalia meta sempre que mudar o mês/categoria selecionados
  useEffect(() => {
    if (goalRootId && goalMonth) {
      refetchGoal();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [goalRootId, goalMonth]);

  /** ---------------- ações de categorias (UI com toggle para subcategoria) ---------------- */
  const [newRootName, setNewRootName] = useState("");
  const [newChildName, setNewChildName] = useState("");
  const [createAsChild, setCreateAsChild] = useState(false); // toggle
  const [renameText, setRenameText] = useState("");
  const [moveParentId, setMoveParentId] = useState("");

  async function doCreate() {
    const name = (createAsChild ? newChildName : newRootName).trim();
    if (!name) return;
    try {
      if (createAsChild) {
        if (!selectedCatId) {
          toast({ title: "Selecione a categoria-pai para criar a subcategoria", variant: "destructive" });
          return;
        }
        await createChildCategory(selectedCatId, { name });
        setNewChildName("");
        toast({ title: "Subcategoria criada" });
      } else {
        await createRootCategory({ name });
        setNewRootName("");
        toast({ title: "Categoria raiz criada" });
      }
      await loadCategories();
    } catch (e: any) {
      toast({ title: "Falha ao criar categoria", description: e?.message ?? "Erro", variant: "destructive" });
    }
  }

  async function doRename() {
    if (!selectedCatId || !renameText.trim()) return;
    try {
      await renameCategory(selectedCatId, { newName: renameText.trim() });
      setRenameText("");
      await loadCategories();
      toast({ title: "Categoria renomeada" });
    } catch (e: any) {
      toast({ title: "Falha ao renomear", description: e?.message ?? "Erro", variant: "destructive" });
    }
  }

  async function doDelete() {
    if (!selectedCatId) return;
    try {
      await deleteCategory(selectedCatId);
      setSelectedCatId("");
      await loadCategories();
      toast({ title: "Categoria removida" });
    } catch (e: any) {
      toast({ title: "Falha ao remover", description: e?.message ?? "Erro", variant: "destructive" });
    }
  }

  async function doMove() {
    if (!selectedCatId || !moveParentId) return;
    try {
      await moveCategory(selectedCatId, { newParentId: moveParentId });
      setMoveParentId("");
      await loadCategories();
      toast({ title: "Categoria movida" });
    } catch (e: any) {
      toast({ title: "Falha ao mover", description: e?.message ?? "Erro", variant: "destructive" });
    }
  }

  /** ---------------- métricas ---------------- */
  const totalIncome = report?.totalCredit ?? 0;
  const totalExpenses = report?.totalDebit ?? 0;
  const balance = report?.balance ?? 0;

  return (
    <div className="min-h-screen bg-background p-4 lg:p-8">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Header com usuário e mês */}
        <header className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-4xl font-bold text-foreground">Controle de Gastos</h1>
            <p className="text-muted-foreground">Gerencie suas finanças de forma inteligente</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <Label>Mês</Label>
              <Input
                value={month}
                onChange={(e) => setMonth(e.target.value)}
                placeholder="YYYY-MM"
                className="w-36"
              />
            </div>
            <div className="text-sm text-muted-foreground">{userEmail ? `Logado: ${userEmail}` : ""}</div>
            <Button
              variant="outline"
              onClick={() => {
                clearToken();
                window.location.href = "/login";
              }}
            >
              Sair
            </Button>
          </div>
        </header>

        {/* Cards métrica */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="p-6 bg-gradient-card shadow-elevated border-border">
            <div className="flex items-center space-x-4">
              <div className="p-3 bg-gradient-primary rounded-full">
                <Wallet className="h-6 w-6 text-primary-foreground" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Saldo ({month})</p>
                <p className={`text-2xl font-bold ${balance >= 0 ? "text-green-600" : "text-red-600"}`}>
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
                <p className="text-2xl font-bold text-green-600">
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
                <p className="text-2xl font-bold text-red-600">
                  {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(totalExpenses)}
                </p>
              </div>
            </div>
          </Card>
        </section>

        {/* Layout Principal */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Coluna esquerda */}
          <div className="space-y-8">
            {/* Nova transação */}
            <Card className="p-6 bg-gradient-card shadow-card border-border">
              <div className="flex items-center space-x-2 mb-6">
                <Plus className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-semibold text-foreground">Nova Transação</h2>
              </div>

              <div className="space-y-4">
                <div>
                  <Label htmlFor="description">Descrição</Label>
                  <Input
                    id="description"
                    value={form.description}
                    onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                    placeholder="Ex: Compra no supermercado"
                  />
                </div>

                <div>
                  <Label htmlFor="amount">Valor</Label>
                  <Input
                    id="amount"
                    type="number"
                    value={form.amount}
                    onChange={(e) => setForm((p) => ({ ...p, amount: e.target.value }))}
                    placeholder="0.00"
                    step="0.01"
                    min="0"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <Label>Tipo</Label>
                    <Select value={form.type} onValueChange={(v: "income" | "expense") => setForm((p) => ({ ...p, type: v }))}>
                      <SelectTrigger>
                        <SelectValue placeholder="Selecione o tipo" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="expense">Despesa (DEBIT)</SelectItem>
                        <SelectItem value="income">Receita (CREDIT)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label>Data</Label>
                    <Input
                      type="date"
                      value={form.date}
                      onChange={(e) => setForm((p) => ({ ...p, date: e.target.value }))}
                    />
                  </div>
                </div>

                <div>
                  <Label>Categoria</Label>
                  <Select
                    value={form.categoryId}
                    onValueChange={(value) => setForm((p) => ({ ...p, categoryId: value }))}
                    disabled={loadingCats}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder={loadingCats ? "Carregando..." : "Selecione uma categoria"} />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.map((cat) => (
                        <SelectItem key={cat.id} value={cat.id}>
                          {cat.path || cat.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <Button onClick={handleAddTransaction} className="w-full">
                  <Plus className="h-4 w-4 mr-2" />
                  Adicionar Transação
                </Button>
              </div>
            </Card>

            {/* Metas */}
            <Card className="p-6 bg-gradient-card shadow-card border-border">
              <h2 className="text-xl font-semibold mb-4">Metas de Gastos</h2>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div className="space-y-2">
                  <Label>Categoria raiz</Label>
                  <Select value={goalRootId} onValueChange={setGoalRootId} disabled={loadingCats}>
                    <SelectTrigger>
                      <SelectValue placeholder={loadingCats ? "Carregando..." : "Selecione a categoria raiz"} />
                    </SelectTrigger>
                    <SelectContent>
                      {categories
                        .filter((c) => !c.parentId)
                        .map((c) => (
                          <SelectItem key={c.id} value={c.id}>
                            {c.path || c.name}
                          </SelectItem>
                        ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Mês (YYYY-MM)</Label>
                  <Input value={goalMonth} onChange={(e) => setGoalMonth(e.target.value)} placeholder="2025-11" />
                </div>

                <div className="space-y-2">
                  <Label>Limite (R$)</Label>
                  <Input
                    type="number"
                    step="0.01"
                    min="0"
                    value={goalLimit}
                    onChange={(e) => setGoalLimit(e.target.value)}
                    placeholder="800.00"
                  />
                </div>
              </div>

              <Button onClick={handleSetGoal}>Definir/Ajustar Meta</Button>

              <div className="mt-6">
                {goalRootId ? (
                  loadingGoal ? (
                    <div className="text-sm text-muted-foreground">Calculando avaliação…</div>
                  ) : goalEval ? (
                    <div className="space-y-2">
                      <div className="text-sm">
                        Limite:{" "}
                        <span className="font-medium">
                          {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(goalEval.limit)}
                        </span>
                      </div>
                      <div className="text-sm">
                        Gasto:{" "}
                        <span className="font-medium">
                          {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(goalEval.spent)}
                        </span>
                      </div>
                      <div className={`text-sm font-semibold ${goalEval.exceeded ? "text-red-600" : "text-green-600"}`}>
                        {goalEval.exceeded ? "Meta EXCEDIDA" : "Dentro da meta"} — Diferença:{" "}
                        {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(goalEval.diff)}
                      </div>
                    </div>
                  ) : (
                    <div className="text-sm text-muted-foreground">Sem dados para a combinação selecionada.</div>
                  )
                ) : (
                  <div className="text-sm text-muted-foreground">Selecione uma categoria raiz para ver a avaliação.</div>
                )}
              </div>
            </Card>

          {/* Gerenciador de categorias (toggle para subcategoria) */}
          <Card className="p-6 bg-gradient-card shadow-card border-border">
            <h2 className="text-xl font-semibold mb-4">Categorias</h2>

            <div className="grid gap-3">
              {/* Toggle */}
              <div className="flex items-center gap-2">
                <input
                  id="toggle-child"
                  type="checkbox"
                  checked={createAsChild}
                  onChange={(e) => setCreateAsChild(e.target.checked)}
                />
                <Label htmlFor="toggle-child">Criar como Subcategoria</Label>
              </div>

              {/* Campo de criação: raiz OU subcategoria */}
              <div className="flex gap-2 items-end">
                {!createAsChild ? (
                  <>
                    <div className="flex-1">
                      <Label>Nova raiz</Label>
                      <Input
                        value={newRootName}
                        onChange={(e) => setNewRootName(e.target.value)}
                        placeholder="Ex.: Alimentação"
                      />
                    </div>
                    <Button onClick={doCreate} disabled={!newRootName.trim()}>
                      <FolderPlus className="w-4 h-4 mr-2" />
                      Criar raiz
                    </Button>
                  </>
                ) : (
                  <>
                    <div className="flex-1">
                      <Label>Categoria pai</Label>
                      <Select
                        value={selectedCatId}
                        onValueChange={setSelectedCatId}
                        disabled={loadingCats}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Selecione o pai" />
                        </SelectTrigger>
                        <SelectContent>
                          {categories.map((c) => (
                            <SelectItem key={c.id} value={c.id}>
                              {c.path || c.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="flex-1">
                      <Label>Nome da subcategoria</Label>
                      <Input
                        value={newChildName}
                        onChange={(e) => setNewChildName(e.target.value)}
                        placeholder="Ex.: Restaurante"
                      />
                    </div>
                    <Button onClick={doCreate} disabled={!selectedCatId || !newChildName.trim()}>
                      <CornerDownRight className="w-4 h-4 mr-2" />
                      Criar Subcategoria
                    </Button>
                  </>
                )}
              </div>

              {/* ------------ NOVO: Selecionar categoria-alvo para RENOMEAR / REMOVER / MOVER ------------ */}
              <div className="flex gap-2 items-end">
                <div className="flex-1">
                  <Label>Selecionar categoria</Label>
                  <Select
                    value={selectedCatId}
                    onValueChange={setSelectedCatId}
                    disabled={loadingCats}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Escolha a categoria" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={c.id}>
                          {c.path || c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Renomear / Remover (usa a seleção acima) */}
              <div className="flex gap-2 items-end">
                <div className="flex-1">
                  <Label>Renomear Categoria</Label>
                  <Input
                    value={renameText}
                    onChange={(e) => setRenameText(e.target.value)}
                    placeholder="Novo nome"
                    disabled={!selectedCatId}
                  />
                </div>
                <Button onClick={doRename} disabled={!selectedCatId || !renameText.trim()}>
                  <Pencil className="w-4 h-4 mr-2" />
                  Renomear
                </Button>
                <Button
                  variant="destructive"
                  onClick={doDelete}
                  disabled={!selectedCatId}
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Remover
                </Button>
              </div>

              {/* Mover: escolher novo pai (fonte = selectedCatId) */}
              <div className="flex gap-2 items-end">
                <div className="flex-1">
                  <Label>Mover “Selecionada” para dentro de…</Label>
                  <Select
                    value={moveParentId}
                    onValueChange={setMoveParentId}
                    disabled={!selectedCatId}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Escolha o novo pai" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories
                        .filter((c) => c.id !== selectedCatId) // evita mover para si mesma
                        .map((c) => (
                          <SelectItem key={c.id} value={c.id}>
                            {c.path || c.name}
                          </SelectItem>
                        ))}
                    </SelectContent>
                  </Select>
                </div>
                <Button onClick={doMove} disabled={!selectedCatId || !moveParentId}>
                  <MoveRight className="w-4 h-4 mr-2" />
                  Mover
                </Button>
              </div>
            </div>
          </Card>
          </div>

          {/* Coluna direita: relatório */}
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
                        <p className="font-semibold text-red-600">
                          {new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(it.debit)}
                        </p>
                      </div>
                      <div className="flex items-center justify-between mt-1">
                        <p className="text-sm text-muted-foreground">Créditos</p>
                        <p className="text-sm text-green-600">
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