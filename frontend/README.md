# 💰 Controle de Gastos — Sistema de Verificação, Validação e Teste

Sistema de **gestão financeira pessoal** desenvolvido como projeto acadêmico para a disciplina de **Verificação, Validação e Teste de Software (VV&T)**.  
O foco é aplicar **boas práticas de arquitetura**, **testes automatizados** e **integração entre front-end e back-end** com autenticação JWT.

---

## 🧠 Visão Geral

A aplicação permite:
- Registro e autenticação de usuários com JWT  
- Criação e organização hierárquica de categorias de gastos  
- Registro de despesas e metas mensais  
- Geração de relatórios por período e por categoria  

---

## ⚙️ Tecnologias Principais

### **Frontend**
- [Vite](https://vitejs.dev/) + [React 18](https://react.dev/)
- [TypeScript](https://www.typescriptlang.org/)
- [Tailwind CSS](https://tailwindcss.com/) + [shadcn/ui](https://ui.shadcn.com/)
- [React Router DOM](https://reactrouter.com/)
- [TanStack Query](https://tanstack.com/query)

### **Backend**
- [Spring Boot 3](https://spring.io/projects/spring-boot)
- [Spring Security + JWT](https://spring.io/projects/spring-security)
- [Springdoc OpenAPI](https://springdoc.org/)
- [SQLite](https://www.sqlite.org/) via JPA/Hibernate


---

## 🚀 Como Executar o Projeto

### 🔹 1. Backend (API)

```bash
# Na raiz do projeto
./mvnw spring-boot:run

A API será executada em:
👉 http://localhost:8080/swagger-ui/index.html

🔹 2. Frontend (App Web)

```bash
cd frontend
npm install
npm run dev
```

A interface estará em:
👉 http://localhost:5173/

Caso queira expor na rede local:

```bash
npm run dev:host
```

🧪 Testes

Executar todos os testes automatizados (frontend):

```bash
npm run test
```

🔐 Endpoints Principais (Swagger)

| Método   | Rota                                     | Descrição                      |
| -------- | ---------------------------------------- | ------------------------------ |
| `POST`   | `/api/v1/register`                       | Registro de novo usuário       |
| `POST`   | `/api/v1/authenticate`                   | Autenticação e retorno de JWT  |
| `GET`    | `/api/v1/categories`                     | Lista de categorias do usuário |
| `POST`   | `/api/v1/categories`                     | Cria categoria raiz            |
| `PATCH`  | `/api/v1/categories/{id}/rename`         | Renomeia categoria             |
| `PATCH`  | `/api/v1/categories/{id}/move`           | Move categoria para outro pai  |
| `DELETE` | `/api/v1/categories/{id}`                | Exclui categoria               |
| `POST`   | `/api/v1/categories/{parentId}/children` | Cria subcategoria              |
| `POST`   | `/api/v1/expenses`                       | Registra uma despesa           |
| `GET`    | `/api/v1/goals/evaluate`                 | Avalia meta mensal             |

🧰 Estrutura de Pastas (Frontend)

frontend/
 ├─ src/
 │   ├─ lib/              # Configurações utilitárias (api, session, utils)
 │   ├─ services/         # Consumo da API (auth, categories, etc.)
 │   ├─ pages/            # Páginas principais (Login, Register, Dashboard)
 │   ├─ routes/           # Regras de rota protegida
 │   ├─ components/       # Componentes reutilizáveis
 │   └─ AppRouter.tsx     # Definição das rotas da aplicação
 ├─ index.html
 ├─ vite.config.ts
 └─ package.json