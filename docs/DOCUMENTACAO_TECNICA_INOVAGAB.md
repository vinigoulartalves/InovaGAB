# InovaGAB — Documentação Técnica

**Projeto:** InovaGAB (Plataforma Mobile de Inovação Corporativa)  
**Programa:** Challenge FIAP + Grupo Águia Branca  
**Autor:** Vinícius Goulart Alves  
**Versão do documento:** 1.0  
**Data:** 26 de maio de 2026

---

## 1. Introdução

O **InovaGAB** é um aplicativo Android nativo concebido para estruturar o ciclo de inovação corporativa no contexto operacional do Grupo Águia Branca. A proposta central é transformar observações do cotidiano em ações estratégicas mensuráveis, promovendo participação da base operacional, governança dos gestores e visibilidade executiva para a liderança.

A solução foi projetada com foco em simplicidade operacional, rastreabilidade de ideias e mensuração de impacto por meio de indicadores de projetos.

---

## 2. Objetivo geral

Implementar uma plataforma mobile capaz de:

- Capturar ideias e problemas do dia a dia dos operadores;
- Permitir priorização e decisão gerencial sobre ideias;
- Converter ideias aprovadas em projetos com acompanhamento de status e impacto;
- Fornecer indicadores estratégicos para suporte à tomada de decisão da liderança.

---

## 3. Problema de negócio

No cenário corporativo, iniciativas de melhoria contínua frequentemente sofrem com:

- Baixa formalização das ideias vindas da operação;
- Dificuldade de priorização consistente;
- Falta de transparência sobre andamento e resultados;
- Ausência de mecanismos de incentivo ao engajamento;
- Visão fragmentada entre operação, gestão e liderança.

Esse contexto reduz a taxa de transformação de boas ideias em projetos efetivos e diminui a capacidade de mensurar retorno de inovação.

---

## 4. Solução proposta

O InovaGAB endereça o problema por meio de um fluxo digital completo:

1. **Operador** registra ideias/problemas no app;
2. **Gestor** avalia, prioriza e aprova/rejeita ideias;
3. Ideias aprovadas podem evoluir para **projetos**;
4. **Liderança** acompanha resultados consolidados em dashboard;
5. Sistema de **pontuação e ranking** incentiva participação contínua.

Essa abordagem cria governança ponta a ponta, com dados estruturados e responsabilidades claras por perfil de usuário.

---

## 5. Arquitetura da solução

### 5.1 Visão arquitetural

- Aplicação **Android nativa** em Kotlin;
- Interface em **Jetpack Compose**;
- Navegação por **Navigation Compose**;
- Persistência e identidade no ecossistema **Firebase**;
- Organização interna em **MVVM simplificado + Repository Pattern**.

### 5.2 Camadas do aplicativo

| Camada | Responsabilidade técnica |
|---|---|
| `ui` | Telas Compose, estados de UI, interação com ViewModels |
| `data` | Models de domínio e repositórios de acesso ao Firebase |
| `core` | Navegação, sessão e componentes visuais reutilizáveis |
| `repository`* | Abstrações de acesso a dados (no projeto, implementadas em `data/repository`) |
| `models`* | Entidades de domínio (no projeto, implementadas em `data/model`) |

> \\* Observação: a separação conceitual solicitada é atendida no código pelas pastas `data/model` e `data/repository`.

---

## 6. Tecnologias utilizadas

| Tecnologia | Uso no projeto |
|---|---|
| Kotlin | Linguagem principal do app |
| Android Studio | IDE de desenvolvimento |
| Jetpack Compose | Construção declarativa de interface |
| Firebase Authentication | Autenticação de usuários |
| Firebase Firestore | Banco NoSQL em nuvem |
| Navigation Compose | Gerenciamento de rotas e fluxo entre telas |
| MVVM simples | Separação UI x estado/regra de apresentação |
| Repository Pattern | Encapsulamento de acesso a dados remotos |

---

## 7. Estrutura de pastas do projeto

```text
app/src/main/java/com/fiap/inovagab/
├── core/
│   ├── navigation/
│   ├── session/
│   └── ui/
│       ├── components/
│       └── effects/
├── data/
│   ├── model/
│   └── repository/
└── ui/
    ├── login/
    ├── operador/
    ├── gestor/
    ├── lider/
    └── shared/
```

### Justificativa técnica da organização

- **Modularidade lógica:** separa responsabilidades por domínio funcional;
- **Manutenibilidade:** reduz acoplamento entre UI e dados;
- **Escalabilidade:** facilita evolução por perfil/tela sem impacto transversal excessivo.

---

## 8. Fluxo de autenticação

### 8.1 Etapas

1. Usuário informa credenciais na tela de login;
2. `AuthRepository` autentica via Firebase Authentication;
3. Após autenticação, sistema carrega dados do usuário (incluindo perfil);
4. `SessionManager` mantém usuário corrente em memória de sessão;
5. Navegação redireciona para a home correspondente ao perfil.

### 8.2 Controle de acesso por perfil

Após o login, o perfil (`OPERADOR`, `GESTOR`, `LIDER`) determina:

- Menu inicial apresentado;
- Acesso às rotas e funcionalidades disponíveis;
- Restrições explícitas em telas sensíveis (ex.: dashboard da liderança).

---

## 9. Fluxo do sistema

### 9.1 Fluxo macro

- **Entrada:** autenticação;
- **Operação:** cadastro e gestão de ideias;
- **Conversão:** criação/atualização de projetos;
- **Monitoramento:** dashboard e ranking;
- **Saída:** logout.

### 9.2 Jornada resumida

1. Login;
2. Acesso à home por perfil;
3. Execução de ações permitidas;
4. Atualização de dados no Firestore;
5. Leitura consolidada para indicadores e ranking.

---

## 10. Descrição detalhada de cada perfil

## 10.1 OPERADOR

**Objetivo:** registrar contribuições da operação e acompanhar retorno.

**Permissões:**
- Consultar orientações estratégicas;
- Cadastrar ideias/problemas;
- Consultar status das próprias ideias;
- Visualizar ranking de engajamento.

## 10.2 GESTOR

**Objetivo:** atuar como curadoria e priorização da inovação.

**Permissões:**
- Consultar orientações;
- Gerenciar ideias enviadas;
- Priorizar ideias;
- Aprovar/rejeitar ideias;
- Criar projetos;
- Atualizar projetos.

## 10.3 LIDER

**Objetivo:** governança estratégica e acompanhamento de resultados.

**Permissões:**
- Gerenciar orientações estratégicas;
- Consultar projetos;
- Visualizar dashboard com indicadores;
- Consultar ranking.

---

## 11. Descrição das telas

| Tela | Perfil principal | Finalidade |
|---|---|---|
| Login | Todos | Autenticar usuário |
| Home Operador | Operador | Acesso rápido às funções operacionais |
| Formulário de Ideia | Operador | Registrar ideia/problema |
| Minhas Ideias | Operador | Acompanhar evolução das ideias |
| Home Gestor | Gestor | Central de gestão de inovação |
| Gestão de Ideias | Gestor | Ajustar prioridade e status |
| Formulário de Projeto | Gestor | Criar/editar projetos |
| Home Líder | Líder | Central executiva |
| Lista de Orientações | Compartilhada* | Consultar orientações (CRUD para liderança) |
| Formulário de Orientação | Líder | Criar/editar orientação estratégica |
| Lista de Projetos | Gestor/Líder | Consultar projetos e editar quando aplicável |
| Dashboard | Líder | Acompanhar KPIs de inovação |
| Ranking | Todos | Visualizar pontuação por engajamento |

> *A tela de lista é acessível por múltiplos perfis; ações de criação/edição são condicionadas ao fluxo funcional.

---

## 12. Estrutura do Firebase

## 12.1 Firebase Authentication

Responsável por:
- Login de usuários;
- Identificação segura por `uid`;
- Base para autorização por perfil no app.

## 12.2 Cloud Firestore

Banco NoSQL principal com coleções:
- `users`
- `ideias`
- `projetos`
- `orientacoes`

A modelagem privilegia documentos independentes para leitura e escrita simples por contexto funcional.

---

## 13. Explicação das collections

## 13.1 `users`

Armazena dados de identidade e engajamento.

| Campo | Tipo | Descrição |
|---|---|---|
| `nome` | String | Nome do usuário |
| `email` | String | E-mail corporativo/login |
| `perfil` | String/Enum | `OPERADOR`, `GESTOR` ou `LIDER` |
| `pontos` | Number | Pontuação acumulada no ranking |

## 13.2 `ideias`

Armazena submissões operacionais e estado de avaliação.

| Campo | Tipo | Descrição |
|---|---|---|
| `titulo` | String | Título da ideia |
| `descricao` | String | Detalhamento |
| `area` | String | Área de aplicação |
| `status` | String/Enum | Estado no funil de análise |
| `prioridade` | String/Enum | Nível de prioridade |

## 13.3 `projetos`

Armazena execução das ideias convertidas em iniciativas.

| Campo | Tipo | Descrição |
|---|---|---|
| `nome` | String | Nome do projeto |
| `descricao` | String | Escopo do projeto |
| `etapa` | String | Fase atual |
| `status` | String/Enum | Situação de execução |
| `investimento` | Number | Valor investido |
| `retornoFinanceiro` | Number | Retorno financeiro estimado/real |
| `ganhoProdutividade` | Number | Percentual de ganho de produtividade |

## 13.4 `orientacoes`

Armazena direcionadores estratégicos para alinhamento das propostas.

| Campo | Tipo | Descrição |
|---|---|---|
| `titulo` | String | Tema da orientação |
| `descricao` | String | Conteúdo orientativo |

---

## 14. Explicação da arquitetura MVVM

O projeto adota **MVVM simplificado**, com separação clara entre apresentação e lógica de estado.

### Papéis

- **View (Compose Screens):** renderiza estado e envia intenções de usuário;
- **ViewModel:** concentra estado de tela (`UiState`), orquestra ações e validações de fluxo;
- **Model (entidades + repositórios):** representa dados e integração externa.

### Benefícios técnicos

- Maior testabilidade da lógica de apresentação;
- Redução de lógica dentro das telas;
- Melhor previsibilidade de estados assíncronos (carregando/sucesso/erro).

---

## 15. Explicação do Repository Pattern

Os repositórios atuam como **camada de abstração de dados** entre ViewModels e Firebase.

### Responsabilidades

- Encapsular chamadas ao Firestore/Auth;
- Centralizar transformações documento ↔ modelo;
- Retornar `Result` para padronizar sucesso/falha;
- Isolar detalhes de infraestrutura da camada de UI.

### Resultado arquitetural

- ViewModels não dependem diretamente de SDK Firebase;
- Facilidade para evolução futura (cache local, testes com mocks, troca de backend).

---

## 16. Fluxo de navegação

As rotas são definidas com Navigation Compose e incluem:

- `login`
- `home_operador`, `ideia_form`, `minhas_ideias`
- `home_gestor`, `gestao_ideias`, `projeto_form`
- `home_lider`, `dashboard`
- `orientacoes_list`, `orientacao_form`
- `projetos_list`
- `ranking`

### Regras de roteamento

- Login redireciona dinamicamente para a home por perfil;
- Logout limpa sessão e remove histórico de navegação;
- Formulários de projeto/orientação aceitam modo novo/edição por parâmetro de rota.

---

## 17. Regras de negócio

1. **Controle de acesso por perfil** em funcionalidades e menus.
2. **Pontuação por cadastro de ideia:** +10 pontos ao autor.
3. **Pontuação por aprovação de ideia:** +30 pontos ao autor.
4. **Não duplicação de pontuação de aprovação:** pontos de aprovação só são somados quando a ideia passa para `APROVADA` sem já estar aprovada.
5. **Gestão de ideias:** gestor define prioridade e status no ciclo de avaliação.
6. **Dashboard restrita à liderança** para leitura estratégica consolidada.

---

## 18. Dashboard e indicadores

A dashboard consolida métricas de projetos para visão executiva.

### Indicadores calculados

- Total de projetos;
- Investimento total;
- Retorno total;
- Lucro obtido;
- ROI geral;
- Redução de custos;
- Ganho médio de produtividade.

### Fórmulas conceituais

- **Lucro obtido:** `retorno total - investimento total`;
- **ROI geral (%):** `(lucro obtido / investimento total) * 100` (quando investimento > 0).

### Justificativa técnica

Os indicadores são derivados de dados transacionais de projetos, permitindo leitura sintética de performance de inovação com foco financeiro e operacional.

---

## 19. Diferenciais e inovação

### 19.1 Gamificação orientada a resultado

O sistema de pontuação e ranking promove comportamento desejado:

- Incentiva o envio contínuo de ideias;
- Reforça qualidade por meio de pontuação adicional na aprovação;
- Dá transparência ao engajamento individual.

### 19.2 Governança integrada em três níveis

- **Operação:** origem das oportunidades;
- **Gestão:** filtro e execução;
- **Liderança:** estratégia e mensuração.

Esse desenho reduz lacunas de comunicação e acelera o ciclo de inovação corporativa.

---

## 20. Considerações finais

O InovaGAB materializa uma arquitetura Android moderna, com Firebase e Compose, adequada ao contexto de inovação corporativa orientada por dados. A combinação de controle por perfil, fluxo estruturado de ideias e dashboard de resultados entrega valor tático e estratégico, ao mesmo tempo em que mantém simplicidade de uso para os diferentes públicos da organização.

Como próximos passos evolutivos (fora do escopo implementado), recomenda-se aprofundar testes automatizados, observabilidade e trilha de auditoria para fortalecer governança em escala.
