# Cenários de Teste Detalhados - Controle de Gastos

## US-01: Registrar Nova Despesa

### C01/US01: Registrar despesa com sucesso (Happy Path)
- **Cenário**: Usuário registra uma despesa válida
- **Pré-condições**: Sistema inicializado, categorias disponíveis
- **Passos**:
  1. Preencher descrição válida (3-100 caracteres)
  2. Inserir valor positivo > 0
  3. Selecionar categoria existente
  4. Opcionalmente selecionar subcategoria válida
  5. Clicar em "Adicionar Transação"
- **Resultado Esperado**: 
  - Transação adicionada à lista
  - Métricas atualizadas corretamente
  - Formulário limpo
  - Toast de sucesso exibido

### C02/US01: Registrar receita com sucesso (Alternative Path)
- **Cenário**: Usuário registra uma receita válida
- **Passos**: Similar ao C01, mas selecionando tipo "Receita"
- **Resultado Esperado**: Valor positivo no saldo, categorizado corretamente

### C03/US01: Tentar registrar despesa com descrição vazia ou nula (Negative)
- **Cenário**: Validação de campo obrigatório
- **Passos**:
  1. Deixar descrição vazia/nula/apenas espaços
  2. Preencher demais campos válidos
  3. Tentar submeter
- **Resultado Esperado**: 
  - Erro "Descrição é obrigatória"
  - Transação não adicionada
  - Formulário não limpo

### C04/US01: Tentar registrar despesa com categoria inexistente (Negative)
- **Cenário**: Validação de integridade referencial
- **Passos**:
  1. Tentar selecionar categoria não existente no sistema
  2. Preencher demais campos válidos
- **Resultado Esperado**: 
  - Erro "Categoria selecionada não existe"
  - Transação não adicionada

### C05/US01: Registrar despesa e verificar estado do agregado (Happy Path)
- **Cenário**: Verificação de integridade dos cálculos
- **Pré-condições**: Estado conhecido de receitas/despesas
- **Passos**:
  1. Registrar nova despesa
  2. Verificar atualização das métricas
- **Resultado Esperado**: 
  - Saldo = receitas - despesas
  - Total de despesas incrementado corretamente
  - Percentuais corretos para análises

## US-02: Categorização Customizável

### C01/US02: Criar categoria com sucesso (Happy Path)
- **Cenário**: Sistema aceita categorias predefinidas
- **Passos**:
  1. Selecionar categoria da lista disponível
  2. Registrar transação
- **Resultado Esperado**: Transação categorizada corretamente

### C02/US02: Criar subcategoria com sucesso (Alternative)
- **Cenário**: Sistema aceita subcategorias válidas
- **Passos**:
  1. Selecionar categoria principal
  2. Selecionar subcategoria válida para essa categoria
  3. Registrar transação
- **Resultado Esperado**: 
  - Transação com categoria e subcategoria
  - Hierarquia mantida

### C03/US02: Tentar usar subcategoria inválida (Negative)
- **Cenário**: Validação de hierarquia categoria-subcategoria
- **Passos**:
  1. Selecionar categoria "Alimentação"
  2. Tentar usar subcategoria de "Transporte"
- **Resultado Esperado**: Erro de validação

## US-03: Análise e Visualização de Gastos

### C01/US03: Calcular métricas corretamente (Happy Path)
- **Cenário**: Sistema calcula e exibe métricas precisas
- **Pré-condições**: Transações de diferentes tipos
- **Passos**:
  1. Adicionar receitas e despesas
  2. Verificar cálculos no dashboard
- **Resultado Esperado**: 
  - Total de receitas correto
  - Total de despesas correto
  - Saldo = receitas - despesas
  - Formatação monetária brasileira

### C02/US03: Visualizar distribuição por categoria (Happy Path)
- **Cenário**: Análise de gastos por categoria
- **Passos**:
  1. Registrar transações em diferentes categorias
  2. Verificar agrupamento por categoria
- **Resultado Esperado**: Gastos agrupados corretamente

### C03/US03: Análise de período temporal (Alternative)
- **Cenário**: Visualização temporal das transações
- **Passos**: Verificar ordenação por data
- **Resultado Esperado**: Transações ordenadas cronologicamente

## US-04: Gerenciamento de Metas de Gastos

### C01/US04: Acompanhar progresso da meta (Happy Path)
- **Cenário**: Sistema atualiza progresso das metas
- **Pré-condições**: Metas definidas para categorias
- **Passos**:
  1. Adicionar despesas em categorias com metas
  2. Verificar atualização do progresso
- **Resultado Esperado**: 
  - Percentual correto (gasto/limite * 100)
  - Status atualizado (ok/warning/critical)

### C02/US04: Meta em estado de alerta (Alternative)
- **Cenário**: Meta entre 80-99% do limite
- **Passos**: Adicionar despesas até atingir 80-99%
- **Resultado Esperado**: 
  - Status "warning"
  - Indicador visual amarelo/laranja
  - Percentual correto

### C03/US04: Meta estourada (Critical Path)
- **Cenário**: Meta acima de 100% do limite
- **Passos**: Adicionar despesas além do limite
- **Resultado Esperado**: 
  - Status "critical"
  - Indicador visual vermelho
  - Percentual >= 100%

### C04/US04: Meta em dia (Happy Path)
- **Cenário**: Meta abaixo de 80% do limite
- **Passos**: Manter gastos baixos
- **Resultado Esperado**: 
  - Status "ok"
  - Indicador visual verde
  - Percentual < 80%

## Cenários de Integração

### CI01: Fluxo completo de transação
- **Cenário**: Usuário completa ciclo completo
- **Passos**:
  1. Adicionar receita
  2. Adicionar despesa
  3. Verificar métricas
  4. Verificar metas
  5. Remover transação
  6. Verificar atualizações
- **Resultado Esperado**: Sistema mantém consistência

### CI02: Validação de integridade dos dados
- **Cenário**: Verificar consistência após múltiplas operações
- **Passos**:
  1. Realizar várias transações
  2. Calcular métricas manualmente
  3. Comparar com sistema
- **Resultado Esperado**: Valores idênticos

### CI03: Teste de responsividade
- **Cenário**: Interface adapta a diferentes telas
- **Passos**: Testar em desktop, tablet, mobile
- **Resultado Esperado**: Layout responsivo funcional

## Casos Extremos (Edge Cases)

### E01: Valores muito grandes
- **Cenário**: Testar limites do sistema
- **Valores**: R$ 999.999.999,00
- **Resultado Esperado**: Sistema rejeita valores acima do limite

### E02: Caracteres especiais em descrição
- **Cenário**: Validação de input
- **Valores**: Emojis, caracteres especiais
- **Resultado Esperado**: Sistema aceita ou rejeita consistentemente

### E03: Múltiplas transações simultâneas
- **Cenário**: Teste de concorrência
- **Passos**: Adicionar várias transações rapidamente
- **Resultado Esperado**: Todas processadas corretamente

## Testes de Performance

### P01: Lista com muitas transações
- **Cenário**: Sistema com 1000+ transações
- **Resultado Esperado**: Interface responsiva

### P02: Cálculos com muitos dados
- **Cenário**: Métricas com grandes volumes
- **Resultado Esperado**: Cálculos rápidos e precisos

## Testes de Acessibilidade

### A01: Navegação por teclado
- **Cenário**: Usuário navega apenas com teclado
- **Resultado Esperado**: Todos elementos acessíveis

### A02: Leitores de tela
- **Cenário**: Uso com tecnologias assistivas
- **Resultado Esperado**: Conteúdo interpretável

### A03: Contraste de cores
- **Cenário**: Validação visual
- **Resultado Esperado**: Contraste adequado (WCAG)