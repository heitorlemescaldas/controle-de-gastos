# Controle de Gastos - Sistema de Verificação, Validação e Teste

## Visão Geral

Sistema de controle financeiro pessoal desenvolvido como projeto acadêmico para a disciplina de **Verificação, Validação e Teste de Software**. O projeto implementa as melhores práticas de VV&T com cobertura completa de testes automatizados.

## 🎯 User Stories Implementadas

### US-01: Registrar Nova Despesa
- ✅ Validação robusta de campos obrigatórios
- ✅ Verificação de integridade dos dados
- ✅ Atualização automática de métricas

### US-02: Categorização Customizável
- ✅ Sistema hierárquico de categorias/subcategorias
- ✅ Validação de integridade referencial

### US-03: Análise e Visualização de Gastos
- ✅ Dashboard com métricas em tempo real
- ✅ Cálculos precisos de receitas/despesas

### US-04: Gerenciamento de Metas de Gastos
- ✅ Sistema de alertas por categoria
- ✅ Indicadores visuais de status

## 🧪 Testes Implementados

### Estrutura Completa de Testes
- **Validação**: Testes unitários para todas as validações
- **Cálculos**: Verificação de métricas financeiras
- **Hooks**: Testes de lógica de negócio
- **Cenários**: Cobertura das User Stories
- **Integração**: Testes E2E da aplicação

### Cenários Cobertos
- ✅ C03/US01: Descrição vazia (Negative)
- ✅ C04/US01: Categoria inexistente (Negative)  
- ✅ C05/US01: Estado do agregado (Happy Path)

## 🚀 Como Executar

```bash
# Instalar dependências
npm install

# Executar testes
npm run test

# Executar aplicação
npm run dev
```

## Project info

**URL**: https://lovable.dev/projects/5e84cfce-a88c-4642-9e3d-5f50936245a0

## How can I edit this code?

There are several ways of editing your application.

**Use Lovable**

Simply visit the [Lovable Project](https://lovable.dev/projects/5e84cfce-a88c-4642-9e3d-5f50936245a0) and start prompting.

Changes made via Lovable will be committed automatically to this repo.

**Use your preferred IDE**

If you want to work locally using your own IDE, you can clone this repo and push changes. Pushed changes will also be reflected in Lovable.

The only requirement is having Node.js & npm installed - [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating)

Follow these steps:

```sh
# Step 1: Clone the repository using the project's Git URL.
git clone <YOUR_GIT_URL>

# Step 2: Navigate to the project directory.
cd <YOUR_PROJECT_NAME>

# Step 3: Install the necessary dependencies.
npm i

# Step 4: Start the development server with auto-reloading and an instant preview.
npm run dev
```

**Edit a file directly in GitHub**

- Navigate to the desired file(s).
- Click the "Edit" button (pencil icon) at the top right of the file view.
- Make your changes and commit the changes.

**Use GitHub Codespaces**

- Navigate to the main page of your repository.
- Click on the "Code" button (green button) near the top right.
- Select the "Codespaces" tab.
- Click on "New codespace" to launch a new Codespace environment.
- Edit files directly within the Codespace and commit and push your changes once you're done.

## What technologies are used for this project?

This project is built with:

- Vite
- TypeScript
- React
- shadcn-ui
- Tailwind CSS

## How can I deploy this project?

Simply open [Lovable](https://lovable.dev/projects/5e84cfce-a88c-4642-9e3d-5f50936245a0) and click on Share -> Publish.

## Can I connect a custom domain to my Lovable project?

Yes, you can!

To connect a domain, navigate to Project > Settings > Domains and click Connect Domain.

Read more here: [Setting up a custom domain](https://docs.lovable.dev/features/custom-domain#custom-domain)
