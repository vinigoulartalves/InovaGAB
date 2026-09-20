# InovaGAB — Sprint 2: diagnóstico e prompts para o Cursor

Preparado em 20/09/2026. Repositório: https://github.com/vinigoulartalves/InovaGAB
Base analisada: main, commit 9665412ce08110cf2e1f11ded6e5adbb13e9ec9d.

Este guia orienta a implementação no Cursor. Não houve alteração no repositório, execução do aplicativo ou acesso ao Firebase nesta análise. Os requisitos enviados pelo usuário são a referência principal; as regras adicionais abaixo são decisões propostas de implementação.

## 1. Diagnóstico do código

O app é Android nativo: Kotlin 1.9.24, Compose, AGP 8.5.2, Java 17, compile/target SDK 34, minSdk 24. Reaproveitar telas, componentes e organização em ViewModels/repositórios.

| Área | Existente | Criar ou alterar |
|---|---|---|
| Backend | Não existe projeto próprio na árvore | API .NET 8, JWT, autorização e persistência MongoDB |
| Login | Firebase Auth e perfil no Firestore | Login REST, renovação, logout e sessão Android |
| Perfis | OPERADOR, GESTOR e LIDER, homes específicas | Permissões no servidor e verificação de propriedade |
| Estratégias | Orientacao com id, título, descrição e criadoEm; CRUD | Categoria, campanha, vigência, histórico imutável e vínculos |
| Ideias | Cadastro, listagem, prioridade e status | Edição/exclusão próprias, vínculo com estratégia e transições válidas |
| Projetos | Cadastro, consulta, edição e campo ideiaId | Exclusão lógica, estratégia e conversão transacional |
| Conversão | Campo ideiaId existe, mas novo formulário inicia vazio | Selecionar ideia aprovada, converter e impedir duplicidade |
| Dashboard | LiderViewModel calcula totais no Android; tela tem cartões | Endpoints agregados, filtros por estratégia/projeto e gráficos |
| Ranking | Usuários Firestore ordenados por pontos | Endpoint e eventos de pontuação no servidor |
| Pontos | +10 cadastro, +30 aprovação; escritas separadas | Atomicidade e idempotência: falha de pontos hoje é silenciada |
| IA | Ausente | Análise real de ideias para auxiliar o gestor |
| Infra/testes | Não constam Docker, suíte de testes ou pipeline | Compose, seed, testes, scripts, Postman e CI |
| Documentação | docs/DOCUMENTACAO_TECNICA_INOVAGAB.md | Preservar Sprint 1 e acrescentar Sprint 2/entregáveis |

Principais arquivos Android a adaptar, sob app/src/main/java/com/fiap/inovagab/:

- data/model/{User,Ideia,Projeto,Orientacao}.kt e os cinco repositórios em data/repository/.
- core/session/SessionManager.kt e core/navigation/{AppNavGraph,Routes}.kt.
- ui/login/LoginViewModel.kt, incluindo exceções Firebase.
- ui/operador/OperadorViewModel.kt, IdeiaFormScreen.kt e MinhasIdeiasScreen.kt.
- ui/gestor/GestorViewModel.kt, GestaoIdeiasScreen.kt e ProjetoFormScreen.kt.
- ui/lider/LiderViewModel.kt, OrientacaoFormScreen.kt e DashboardScreen.kt.
- Telas compartilhadas, InovaGabApp.kt, Gradle raiz/app e configuração de rede.

O app faz chamadas reais ao Firebase. A Sprint 2 deverá transferir os fluxos para a API própria, incluindo ranking e regras financeiras. Não basta acrescentar um backend que o Android não utiliza.

## 2. Escolhas de arquitetura

Stack proposta: .NET 8 Web API, JWT Bearer, PasswordHasher do ASP.NET Core Identity, EF Core com provedor MongoDB, MongoDB NoSQL, Retrofit/OkHttp no Android e Gemini para IA. Manter Android na raiz; acrescentar backend/, infra/, scripts/, tests/, docs/sprint2/ e deliverables/.

Backend como monólito modular simples: Api, Application, Domain e Infrastructure. Sem microserviços, Kubernetes ou filas desnecessárias.

O enunciado escreve “ASP.NET Identity/JWT”. A proposta usa JWT e hash de senha do Identity; isso não equivale a implementar todo UserManager/RoleManager. Documentar essa escolha. Caso o professor exija Identity completo, será necessário um store compatível com MongoDB.

EF Core deve ser usado efetivamente nos CRUDs, não apenas instalado. Validar versões de .NET/EF/provider antes dos módulos. Driver oficial pode atender índices, agregações e operações atômicas específicas na Infrastructure. Não utilizar SQL Server ou migrations SQL; não usar IdentityDbContext relacional com MongoDB.

MongoDB de desenvolvimento/testes: replica set de um nó, permitindo transações. Não misturar gravações EF e driver em sessões independentes e afirmar que são atômicas.

~~~mermaid
flowchart TD
    A[Android Kotlin e Compose] -->|REST e JWT| B[API .NET 8]
    B --> C[Serviços e regras por perfil]
    C --> D[EF Core e driver MongoDB]
    D --> E[MongoDB]
    C --> F[Cliente de IA]
    F --> G[Gemini API]
~~~

IA escolhida: análise da ideia e sugestão de prioridade conforme estratégia, com notas, justificativa, riscos e melhorias. Aprovação continua humana. Implementar bem esse diferencial; não é necessário implementar as três opções de IA.

Dados: usar novo MongoDB com seed sintético identificado como demonstração. Não apagar/modificar o Firebase. Migração dos dados reais/senhas antigas está fora deste plano e não deve ser presumida.

## 3. Como usar

Salve este arquivo em docs/sprint2/GUIA_CURSOR.md no checkout aberto no Cursor. Envie os 10 prompts abaixo, um de cada vez no modo Agent. Em conversa nova, anexe guia, plano e STATUS. Não enviar tudo de uma vez.

Regras permanentes para o Cursor:

- Ler AGENTS.md aplicável, preservar alterações locais e não fazer push/publicação automática.
- Implementar a etapa, executar verificações disponíveis e corrigir erros; não apenas explicar.
- Não inventar resultados. Separar implementado, executado, aprovado e bloqueado por ambiente.
- Sem mocks em execução normal. Seed é dado real persistido. Doubles só em testes isolados, especialmente testes de falhas de IA.
- Não versionar segredos, tokens, senhas reais, keystores ou .env; não colocá-los em logs/APK/imagem.
- Manter docs/sprint2/STATUS.md com versões, comandos, resultados, pendências e próxima etapa.
- Autor, role, pontos, autorização, conversão e indicadores são controlados no servidor.
- Priorizar funcionalidade (50%), integração (15%), documentação (15%), qualidade (10%) e inovação (10%).

## 4. Contrato funcional comum

### Matriz proposta de acesso

| Operação | OPERADOR | GESTOR | LIDER |
|---|---|---|---|
| Login/refresh/logout/me | Sim | Sim | Sim |
| Estratégias e histórico | Consultar | Consultar | CRUD |
| Ideias | CRUD próprias; editar/excluir só ENVIADA | Consultar todas, avaliar, priorizar | Sem acesso individual por padrão |
| Projetos | Não | CRUD e converter ideia | Consultar |
| Relatórios/dashboard | Não | Não | Consultar |
| Ranking | Consultar | Consultar | Consultar |
| Análises IA | Não | Solicitar/consultar | Não |

Líder não herda automaticamente ações de gestor. Sem cadastro público com seleção de role. Provisionar contas dev por seed, incluindo dois operadores para testar isolamento.

### Entidades, estados e invariantes

1. Usuário: id, nome, email normalizado único, passwordHash, perfil, ativo. Hash nunca sai na API.
2. Estratégia, evolução de orientação: id, titulo, descricao, categoria, campanha, inicioVigencia, fimVigencia opcional, ativa, versao, criadoEm, atualizadoEm, excluidaEm. Histórico imutável de criação/edição/arquivamento com versão, ator, data e snapshot do conteúdo.
3. Vigente: ativa, não excluída e data de negócio dentro do intervalo. Datas de vigência são datas civis; usar America/Sao_Paulo para a data de negócio e UTC para instantes técnicos. Pode haver mais de uma estratégia vigente; usuário seleciona uma.
4. Ideia: campos atuais + estrategiaId, estrategiaVersao, versao de concorrência e exclusão lógica. Autor vem do JWT. Cadastro exige estratégia vigente. Edição/exclusão apenas pelo dono em ENVIADA. DTO do operador não permite autor/status/prioridade/pontos.
5. Transições: ENVIADA → EM_ANALISE/APROVADA/REJEITADA; EM_ANALISE → APROVADA/REJEITADA; REJEITADA → EM_ANALISE por gestor; APROVADA → VIROU_PROJETO só por conversão; VIROU_PROJETO terminal. Repetição do mesmo status é idempotente; transição ilegal retorna 409.
6. Projeto: campos atuais + estrategiaId/versao, ideiaId opcional, responsavelId de gestor ativo, versao de concorrência e exclusão lógica. Dinheiro decimal/Decimal128, prazo como data ISO; valores financeiros não negativos. Definir limites para ganho percentual. Criar diretamente exige estratégia vigente. PUT não relinca origem/estratégia silenciosamente.
7. Conversão: apenas ideia APROVADA; herda estratégia/versão e exige estratégia ainda vigente. Se vencida, retorna conflito explicativo. Criar projeto e mudar ideia para VIROU_PROJETO na mesma transação; índice único parcial por ideiaId não vazio. Um projeto por ideia, inclusive diante de retries/concorrência. Exclusão lógica de projeto não reabre a ideia.
8. Exclusão lógica de estratégia preserva histórico e referências existentes, mas impede novos vínculos. Listagens normais omitem excluídos; consultas históricas continuam recuperando referências autorizadas.
9. Pontuação por eventos únicos (autorId, ideiaId, tipo): CADASTRO +10, PRIMEIRA_APROVACAO +30. Operação e evento atômicos. Ranking agrega eventos; não depende de contador editável pelo app. Repetição/reaprovação/concorrência não duplicam. Nesta versão, exclusão lógica da ideia mantém pontos já concedidos; documentar a decisão.
10. Refresh token aleatório, hash no banco, expiração, rotação de uso único e revogação no logout. JWT curto com sub/role/issuer/audience/exp/jti. PasswordHasher, nunca criptografia reversível de senhas.
11. Auditoria com ator, ação, recurso/id, instante UTC e correlationId, sem segredos. Garantir atomicidade do histórico de estratégias. Logs estruturados e healthchecks, sem exigir stack pesada de observabilidade.
12. Versão esperada para edição/avaliação/conversão; conflito retorna 409. Definir If-Match para exclusão. Validar propriedade em consultas por id e filtros, não só nos menus.

### HTTP

Prefixo /api/v1, JSON camelCase, enums textuais iguais aos Android, IDs string, instantes ISO UTC, datas yyyy-MM-dd, dinheiro JSON numérico. DTOs separados de entidades. Listas: {items,page,pageSize,totalItems,totalPages}, página inicial 1, padrão 20, máximo 100, ordenação estável.

ProblemDetails com code/traceId/erros de campo. 400 validação, 401 sem autenticação, 403 role, 404 inexistente/recurso alheio não revelável, 409 conflito, 429 limite, 502/503/504 falhas externas conforme caso. Documentar comportamento uniforme.

### Rotas mínimas

| Método | Rota após /api/v1 | Finalidade |
|---|---|---|
| POST | /auth/login | Email/senha; accessToken, expiresAt, refreshToken, usuário |
| POST | /auth/refresh | Renovação com rotação |
| POST | /auth/logout | Revogar refresh da sessão, idempotente |
| GET | /auth/me | Perfil autenticado |
| GET | /usuarios/responsaveis | Gestor: lista mínima de gestores ativos |
| GET | /estrategias | Todos; filtros vigente/categoria/campanha |
| GET | /estrategias/{id} | Detalhe/referência arquivada identificada |
| GET | /estrategias/{id}/historico | Histórico paginado |
| POST | /estrategias | Criar, líder |
| PUT | /estrategias/{id} | Editar, líder |
| DELETE | /estrategias/{id} | Exclusão lógica, líder |
| GET | /ideias | Operador próprias/gestor todas; filtros |
| GET | /ideias/{id} | Proprietário ou gestor |
| POST | /ideias | Operador, estratégia vigente |
| PUT | /ideias/{id} | Proprietário, enquanto ENVIADA |
| DELETE | /ideias/{id} | Proprietário, enquanto ENVIADA |
| PATCH | /ideias/{id}/avaliacao | Gestor, status/prioridade/justificativa/versão |
| POST | /ideias/{id}/projeto | Gestor, conversão transacional |
| GET | /projetos | Gestor/líder; filtros |
| GET | /projetos/{id} | Gestor/líder |
| POST | /projetos | Gestor, criação direta; sem contornar conversão |
| PUT | /projetos/{id} | Gestor, atualizar dados/resultados |
| DELETE | /projetos/{id} | Gestor, lógica |
| GET | /relatorios/dashboard | Líder; estrategiaId/projetoId/inicio/fim |
| GET | /relatorios/estrategias | Líder, agrupados por estratégia |
| GET | /relatorios/projetos/{id} | Líder, resultado individual |
| GET | /ranking | Todos; posição/nome/pontos, sem email |
| POST | /ideias/{id}/analises-ia | Gestor, chamada real |
| GET | /ideias/{id}/analises-ia | Gestor, histórico |

Fora do prefixo: /health/live (processo) e /health/ready (Mongo/inicialização). Não depender de IA no readiness do core. O Cursor deverá completar todos os DTOs, exemplos e respostas no contrato/OpenAPI.

### Relatórios

Lucro = retorno − investimento; ROI = lucro/investimento × 100; investimento zero → ROI null (“Não aplicável”). Não somar percentuais de ROI nem contar redução de custos duas vezes no lucro. Ganho de produtividade: média simples documentada. Filtro temporal sobre criação do projeto, explicitado na UI. Projetos excluídos fora dos totais; referências arquivadas preservadas. Atrasado: prazo vencido e status não concluído/cancelado.

Retornar séries prontas para barras investimento x retorno por estratégia e distribuição por status. Não inventar série mensal financeira se não existe histórico de medições.

### IA

Enviar apenas título/área/descrição da ideia e conteúdo da estratégia associada. Retornar JSON validado: pontuacaoTotal, alinhamentoEstrategico, impacto, viabilidade (0–100), prioridadeSugerida (BAIXA/MEDIA/ALTA), justificativa, riscos[] e melhorias[]. Persistir provedor/modelo/data, promptVersion e versão/hash da entrada. Marcar análise desatualizada após edição.

IA não aprova, converte, pontua usuário ou altera prioridade final automaticamente. Conteúdo da ideia é dado não confiável, não instrução. Limitar entrada/saída, timeout e frequência. Chave só no backend. Sem chave/quota → indisponibilidade clara, nunca análise falsa. Teste externo separado deve comprovar a chamada real.

## 5. Prompt 1 — diagnóstico local e contrato

~~~text
Vamos implementar a Sprint 2 do InovaGAB. Leia integralmente docs/sprint2/GUIA_CURSOR.md e AGENTS.md aplicável. Aplique as regras comuns do guia em todas as etapas.

Nesta etapa, compare o checkout com a base 9665412ce08110cf2e1f11ded6e5adbb13e9ec9d, sem reverter mudanças posteriores. Preserve Android e alterações locais; não faça push.

Crie docs/sprint2/PLANO_EXECUCAO.md com arquitetura, matriz de permissões, entidades, transições, propriedade, vigência, histórico, exclusão lógica, concorrência, pontuação e conversão. Use .NET 8 Web API, JWT + PasswordHasher do Identity, EF Core com provedor MongoDB e NoSQL. Explique a distinção entre componentes Identity usados e Identity completo. Sem SQL Server, IdentityDbContext relacional ou EF meramente decorativo.

Crie CONTRATO_API.md e OpenAPI inicial validável com todas as rotas do guia: requests/responses, enums, paginação, validações, erros e exemplos completos, inclusive conversão, relatórios, refresh e IA. Defina versões de concorrência e If-Match para exclusões. Complete DTOs e documente exclusão lógica.

Crie RASTREABILIDADE.md ligando requisito a endpoint, tela, teste e evidência futura. Diferencie obrigatório de Plus. Verifique versões de SDK/EF/provider/Mongo em documentação oficial e proponha versões concretas para validar na etapa 2. Não assuma suporte total a LINQ/transações/Identity. Explique onde o driver será necessário; não misturar sessões independentes fingindo atomicidade.

Crie STATUS.md. Apresente decisões, inconsistências e dependências externas. Nesta etapa não implemente CRUDs nem reescreva Android. Pare ao concluir para eu enviar o próximo prompt.
~~~

## 6. Prompt 2 — fundação, MongoDB e Docker

~~~text
Leia GUIA_CURSOR, PLANO_EXECUCAO, CONTRATO_API e STATUS. Implemente apenas fundação backend/ambiente.

Crie backend/InovaGAB.sln com Api/Application/Domain/Infrastructure, target .NET 8, dependências adequadas, nullable, DI, CancellationToken e configuração por ambiente. Fixe SDK/pacotes compatíveis e lock files. Inclua Swagger/OpenAPI, ProblemDetails, erros centralizados, traceId e logs sem segredos.

Configure MongoDB.EntityFrameworkCore para CRUD real e driver oficial para índices/recursos específicos. Antes dos módulos, execute teste de integração gravando/lendo/atualizando/excluindo documento real via EF Core; verifique Decimal128, enums, IDs, datas, concorrência e transações da combinação selecionada. Registre versões/comandos. Não substituir EF silenciosamente por driver puro ou SQL. Não executar migrations SQL: inicializar índices e versionamento de documentos de forma idempotente.

Crie Dockerfile multi-stage SDK/runtime .NET 8, runtime sem root quando viável, .dockerignore e docker-compose.yml. Serviços mongo, inicializador idempotente de replica set rs0 e api; volume e healthchecks. Esperar eleição de PRIMARY, não só porta aberta. Nome anunciado do nó precisa resolver na rede da api/test-runner. Execução recomendada dos testes dentro do Compose; se oferecer execução no host, validar URI com directConnection apropriada. Mongo não exposto publicamente; debug apenas localhost/profile.

Crie .env.example sem segredos reais; scripts/setup-dev.sh e .ps1 geram .env local com segredo JWT forte e senhas dev, sem sobrescrever arquivo existente. Faça binding .NET explícito. IA opcional para subir o core, configurada por enabled/key/model; nunca fallback falso. Seed desligado em produção.

GET /health/live e /health/ready verificam processo e Mongo/inicialização, respectivamente, sem depender de Gemini. API host localhost:8080 e serviço interno api:8080. Fixar imagens/dependências, sem latest. Explicar que Compose é de avaliação, não de produção.

Execute restore/build, docker compose config/build/up, health e CRUD EF quando o ambiente permitir. Se Docker ausente, registrar bloqueio, não sucesso. Atualize README/STATUS. Pare antes dos módulos de negócio.
~~~

## 7. Prompt 3 — autenticação e autorização

~~~text
Leia os documentos Sprint 2. Implemente login/refresh/logout/me e lista mínima de responsáveis conforme contrato.

Usuário no MongoDB; senha com PasswordHasher<TUser> do Identity. JWT com JwtBearer validando assinatura/expiração/issuer/audience, roles OPERADOR/GESTOR/LIDER e segredo obrigatório externo sem fallback. Access token curto (ex.:15 min), refresh aleatório seguro (ex.:7 dias), apenas hash persistido. Rotação de uso único, revogação no logout e proteção concorrente. Documente que JWT já emitido vale até expirar se não houver revogação adicional; não alegue invalidação instantânea.

Sem cadastro público, role enviada pelo cliente ou hash em resposta/log. Índice único de email normalizado, rate limit de login e erro sem revelar conta existente. Policies seguem guia.

Seed dev idempotente: operador1@inovagab.local, operador2@inovagab.local, gestor@inovagab.local, lider@inovagab.local. Senhas vêm do .env gerado, não código. Não resetar senhas/dados a cada startup. Identificar dados demo.

Testes reais com WebApplicationFactory/Mongo isolado: login válido/inválido, token adulterado/expirado, refresh válido/expirado/reutilizado/concorrente, logout e JSON sem hash. Endpoints de negócio não existentes ficam registrados como pendentes, sem testes verdes fictícios.

Atualize OpenAPI/README/STATUS e execute testes HTTP disponíveis sem expor credenciais. Pare nesta etapa.
~~~

## 8. Prompt 4 — estratégias e ideias

~~~text
Leia guia/contrato/status. Implemente estratégias e ideias com persistência real e permissões.

Estratégias: CRUD da liderança, consulta/histórico para todos. Categoria/campanha/vigência/versão e histórico imutável a cada criação/edição/arquivamento com ator/data/snapshot. Alteração e histórico atômicos: embed validado e limitado, ou transação real. Versão esperada obsoleta →409. Referências antigas continuam disponíveis. Sem exclusão em cascata. Relógio injetável e timezone documentado para vigência.

Ideias: CRUD próprias pelo operador, edição/exclusão apenas ENVIADA. Autor obtido do token. Cadastro/novo vínculo exige estratégia vigente, armazenando id e versão. Gestor consulta todas e avalia/prioriza. Líder não herda acesso individual. Operador não pode enviar autor/status/prioridade/pontos; negar acesso por id/filtros adulterados a ideias alheias. Paginação e filtros reais. Implementar transições do guia, 409 para ilegais e preservar vínculo histórico.

Pontuação: eventos únicos CADASTRO +10 e PRIMEIRA_APROVACAO +30; gravação da ideia/evento e aprovação/evento atômicas. Índice único composto. Retry/reaprovação/concorrência não duplicam. Ranking derivará dos eventos. Não copiar falhas de pontos silenciadas do Android. Operações especiais podem usar driver na Infrastructure, com sessão/transação única real; CRUDs continuam usando EF. Não unir SaveChanges e write independente chamando isso de transação.

Testes: todos CRUDs/roles, propriedade, mass assignment, estratégia vencida/futura/arquivada, limites temporais, histórico, versão obsoleta, edição após aprovação, pontuação repetida/concorrente e rollback de falha parcial. Banco exclusivo de teste. Atualize payloads/respostas, rastreabilidade e STATUS. Pare nesta etapa.
~~~

## 9. Prompt 5 — projetos, relatórios e ranking

~~~text
Leia os documentos Sprint 2. Implemente projetos, conversão, relatórios e ranking.

CRUD de projetos do gestor, consulta do líder; operador negado. Validar responsável gestor ativo, estratégia vigente ao criar, datas/valores/versão. Preservar origem e estratégia no PUT; sem relink silencioso. Exclusão lógica. Criação direta não aceita ideiaId para burlar conversão.

Conversão POST /ideias/{id}/projeto: ideia APROVADA e estratégia ainda vigente, herdar id/versão, criar projeto e mudar para VIROU_PROJETO na mesma transação Mongo. Índice único parcial de ideiaId. Requisições repetidas/concorrentes não duplicam, conflito conforme contrato, falha parcial desfaz tudo. Não chamar IA dentro de transação. Projeto excluído não libera segunda conversão.

Relatórios só para líder: geral, estratégia, projeto, intervalo de criação, gráficos, investimento/retorno/lucro/ROI/redução de custos/produtividade/prazos. ROI zero investimento = null. Não somar ROIs ou duplicar economia no lucro. Filtros consistentes em totais e séries. Não agregar apenas a primeira página. Agregações reais do Mongo no repositório de relatórios. Excluídos fora dos totais e estratégia arquivada preservada como referência.

Ranking agrega eventos, ordena por pontos/nome/id e retorna posição/nome/pontos sem emails.

Amplie seed idempotente com estratégias vigente/vencida, ideias dos dois operadores em vários estados e projetos. Inclua cenário verificável: A investimento1000/retorno1500; B investimento2000/retorno2600; total3000/4100, lucro1100 e ROI36,6667% aproximadamente. Não usar média dos ROIs. Arredondar na apresentação.

Teste CRUD/roles, responsável/vínculo inválido, duplicidade/concorrência/rollback da conversão, exclusão, ranking, dashboard vazio/investimento zero, filtros e valores conhecidos. Atualize documentação/STATUS. Pare.
~~~

## 10. Prompt 6 — IA real

~~~text
Leia guia/contrato/status. Implemente análise e sugestão de prioridade de ideias via Gemini API real.

Consulte documentação oficial atual antes de fixar modelo/endpoint/SDK. Escolha modelo com saída estruturada e faixa gratuita acessível à conta, registre identificador/data/fonte/limites. AI__Model configurável. Não prometer gratuidade ilimitada nem migrar automaticamente para modelo pago. Se chave/quota faltarem, implementar cliente e registrar teste real bloqueado, sem análise fake.

Crie IIdeaAnalysisService e cliente IHttpClientFactory. Chave externa só backend. Enviar dados mínimos da ideia e estratégia/versão, sem nome/email. Texto da ideia é dado não confiável; modelo não tem ferramentas de escrita. JSON de notas/prioridade/justificativa/riscos/melhorias conforme guia, com schema e validação de faixa/enums/tamanhos.

POST /ideias/{id}/analises-ia só GESTOR: carregar recurso autorizado, chamar provedor, persistir análise validada com provedor/modelo/data/promptVersion/hash e versão da entrada. GET histórico paginado. Timeout/cancelamento/rate limit e limites de tokens. Cache por entrada+modelo+prompt quando apropriado. Se ideia mudar durante análise, marcar desatualizada. Não modificar status, pontos ou prioridade final.

Tratar 429/5xx/timeout/chave ausente ou inválida/JSON inválido com ProblemDetails claros sem segredos. Core disponível sem IA. Nenhuma resposta fixa no fluxo normal.

Testes HTTP controlados apenas em ambiente Test para falhas e parsing; teste externo real separado opt-in por chave, validando persistência. Registrar evidência sanitizada com modelo/data/status/campos; se não executou, PENDENTE. Documente configuração local da chave e comandos. Atualize STATUS. Pare.
~~~

## 11. Prompt 7 — Android integrado à API

~~~text
Leia guia/contrato/status e código Android. Preserve Kotlin/Compose e aparência; não reescreva tudo nem atualize dependências indiscriminadamente.

Adicione Retrofit/OkHttp e serializador compatível. DTOs/mappers/ApiServices para todos os módulos. Substitua cinco repositórios Firebase por REST; remova do app cálculos de relatórios e gravações de pontos. Mapeie IDs/enums/datas/valores; BigDecimal para entradas monetárias e serialização numérica correta.

SessionManager/TokenStore com proteção Android Keystore e armazenamento privado do material cifrado, excluído do backup. Interceptor Bearer, refresh sincronizado via cliente separado sem recursão e máximo um retry. 401 definitivo limpa sessão/navega login; 403 não renova em loop. Logout revoga refresh e apaga dados locais. Restauração valida /auth/me. Não logar tokens/senhas.

BASE_URL por build, terminando em /: emulador padrão http://10.0.2.2:8080/; aparelho IP LAN do PC ou adb reverse + http://127.0.0.1:8080/. api:8080 só resolve entre containers. HTTP permitido apenas debug/config de desenvolvimento; release HTTPS. Backend HTTP dev não deve redirecionar para HTTPS inexistente.

Atualize LoginViewModel/exceções, InovaGabApp e inicialização. Remova plugins/dependências Firebase desnecessários após migração e confirme ausência de acessos Auth/Firestore nos fluxos ativos. Não tocar dados Firebase remoto.

Integre primeiro telas existentes: login três perfis, orientações, ideias, gestão, projetos, dashboard/ranking. Loading/vazio/erro/retry, duplo clique e paginação: não limitar visualização aos primeiros20 itens.

Execute ./gradlew assembleDebug e testes disponíveis. Diferencie build de execução em dispositivo. Sem SDK/emulador, registrar bloqueio. Valide serialização/refresh com testes e integração real quando possível. Atualize STATUS. Pare antes de completar novas telas.
~~~

## 12. Prompt 8 — lacunas de interface e gráficos

~~~text
Leia documentos Sprint 2. Complete funcionalidades consumindo API real.

Operador: formulário com estratégia vigente, categoria/campanha/vigência, cadastro/edição. Minhas ideias com vínculo/status e editar/excluir quando permitido; confirmação e tratamento409.

Líder: orientações/estratégias com categoria/campanha/datas/status/histórico. Reaproveite visual, contrato consistente. Referências antigas arquivadas visíveis, sem seleção em novos vínculos.

Gestor: converter ideia aprovada via formulário preenchido e origem/estratégia bloqueadas, chamando endpoint de conversão. Projeto direto seleciona estratégia e responsável da API. Edição/resultados, prazo válido, exclusão com confirmação. Tratar concorrência.

IA: botão Analisar com IA na gestão, loading/erro/indisponibilidade, notas, prioridade sugerida, justificativa/riscos/melhorias, data/modelo e indicador desatualizada. Aplicar prioridade exige decisão explícita via API de avaliação. Falta de chave/quota mostra erro real.

Dashboard: relatórios backend, filtros estratégia/projeto/período de criação, detalhes, barras investimento x retorno por estratégia e distribuição por status. Compose Canvas ou biblioteca compatível, com legendas e alternativa textual acessível. Sem séries inventadas. Null ROI=Não aplicável; vazio diferente de erro.

Atualize rotas/factories/DI e formato pt-BR sem alterar JSON. Ranking REST todos perfis. TestTags estáveis nos controles para automação.

Compile APK, valide jornadas; onde emulador disponível, testes instrumentados e evidências reais. Não inventar screenshots. Atualize STATUS/rastreabilidade. Pare.
~~~

## 13. Prompt 9 — testes reproduzíveis e CI

~~~text
Leia guia/contrato/status. Torne o projeto testável a partir de checkout limpo, sem Android Studio para testar API.

Scripts Bash/PowerShell para setup/up/seed/smoke/testes/evidências; exit code nãozero em falha, wait readiness com timeout, sem sleeps cegos. Nunca usar banco produtivo. Reset só banco explicitamente teste e por comando explícito, nunca startup.

xUnit para regras; WebApplicationFactory e Mongo replica set real isolado para integração. Ofereça docker compose profile tests/test-runner sem montar Docker socket; Testcontainers opcional, não único caminho. Test-runner dentro da rede, banco separado; limpar apenas seu banco. Isolamento para concorrência entre testes. Coletar TRX/JUnit/cobertura em artifacts ignorado no Git.

E2E HTTP com login real: líder cria estratégia; operador1 cria ideia; operador2 não acessa/edita; gestor aprova/converte uma vez/atualiza resultado; líder consulta dashboard; operador vê status/ranking. IA real em suíte opt-in separada. Cobrir CRUD, roles, mass assignment, paginação, JWT/refresh/replay, histórico, datas, exclusão, idempotência, concorrência/rollback e efeitos no banco. Não substituir Mongo por InMemory/SQLite.

Postman collection/environment sem segredos, captura tokens por perfil/ids, asserts, erros e IA; runner opcional e curl. IA sem chave fica explicitamente pulada/pendente, nunca verde falso.

Android: testes de mapeamento/erros/refresh, build APK e roteiro dispositivo. Automação Compose instrumentada ou Maestro para login/criar ideia/consultar, usando API real e testTags; documentar contas, baseURL e emulador. Roteiro manual por perfil com resultados esperados.

GitHub Actions: jobs backend build/test com Mongo replica set; Android lint/test/assembleDebug com Java17/SDK; artefatos. Versões/refs fixados. IA real não roda por padrão em PR, não expõe chave. Não afirmar CI remoto aprovado porque YAML existe.

Execute o disponível, registre comandos/data/commit/versões/contagens/falhas. Crie COMO_TESTAR.md e MATRIZ_TESTES.md. Não remover assertions para obter verde. Atualize STATUS. Pare.
~~~

## 14. Prompt 10 — documentação e entrega

~~~text
Leia todos os docs Sprint 2. Revise requisitos, código, API/app e evidências. Corrija divergências concretas sem ampliar escopo.

README raiz e backend com pré-requisitos/versões/arquitetura, comandos Bash/PowerShell em ordem, .env, seed/perfis, Docker/URLs/Swagger, testes, baseURL emulador/celular, build APK e troubleshooting. Uso efetivo EF/Mongo, limites, sem migrations SQL, novo seed versus Firebase antigo, IA opcional para core e chave/modelo/quota necessários para Plus real.

Documente todos endpoints (rota/método/role/payload/resposta/erros), exporte OpenAPI da API real e compare contrato. Diagramas Mermaid. IA: modelo exato configurado/testado, schema/prompt/limites/evidência ou pendência explícita.

Crie docs/sprint2/APRESENTACAO.md: problema, evolução Sprint1→2, arquitetura backend, NoSQL, segurança, fluxo integrado, exemplos endpoints, gráficos, IA e testes. Campos [NOME] e [RM] para dados ausentes; não inventar. Se houver ferramenta, exportar PDF/PPT legível; senão registrar exportação pendente. Markdown não substitui entrega obrigatória PDF/PPT.

scripts/package-delivery.sh e .ps1 geram deliverables/InovaGAB_Backend_Sprint2.zip e InovaGAB_Android_Sprint2.zip. Backend inclui fontes/solution/infra/scripts/exemplos/README e roda após extração independente. Android inclui wrapper/build/fontes/exemplos/README e APK debug instalável identificado, baseURL e instruções de demonstração. Release não libera HTTP. Build APK deve usar endereço adequado ao ambiente escolhido. Incluir apresentação final quando disponível.

Excluir .git, .env, segredos/keystores/tokens, dumps reais, caches/bin/obj/build intermediários, local.properties e relatórios sensíveis. APK selecionado é exceção aos intermediários. Evitar ZIP dentro de si. Manifest com commit/versões/SHA256 e roteiro. Validar extração em pasta temporária; não afirmar build limpo sem executá-lo.

CHECKLIST_ENTREGA.md: comprovado / implementado não executado / pendente. Destacar nomes/RMs, IA real não testada, APK e apresentação faltantes. Atualize STATUS e relate o que está pronto e o que falta. Não publicar nem submeter na FIAP.
~~~

## 15. Validação posterior

Envie commit atualizado ou ZIPs, APK, COMO_TESTAR.md, .env.example, OpenAPI, relatórios e STATUS. Não enviar chaves reais no chat/ZIP. O ambiente gera próprias credenciais locais.

Com Docker disponível, será possível executar API/Mongo, HTTP/integrados e verificar persistência/regras. Execução Android exige SDK/emulador/aparelho; IA real exige chave/modelo/quota e saída de rede. Se esses recursos faltarem na revisão, não será possível afirmar validação daquela execução.

Comandos esperados, a serem criados pelos prompts (ainda não existem):

~~~bash
bash scripts/setup-dev.sh
docker compose up -d --build
bash scripts/smoke-test.sh
docker compose --profile tests run --rm test-runner
./gradlew testDebugUnitTest assembleDebug
bash scripts/package-delivery.sh
~~~

Manter nomes de scripts e documentação consistentes. Não empacotar como substituto de concluir testes/preencher nomes e RMs.

## 16. Referências

- Código: https://github.com/vinigoulartalves/InovaGAB/tree/9665412ce08110cf2e1f11ded6e5adbb13e9ec9d
- Provider: https://github.com/mongodb/mongo-efcore-provider
- Limitações: https://www.mongodb.com/docs/entity-framework/current/limitations/ (selecionar a versão adotada, pois current pode ser outra).
- Identity stores: https://learn.microsoft.com/en-us/aspnet/core/security/authentication/identity-custom-storage-providers?view=aspnetcore-8.0
- IA estruturada: https://ai.google.dev/gemini-api/docs/structured-output
- Preços: https://ai.google.dev/gemini-api/docs/pricing
- Limites: https://ai.google.dev/gemini-api/docs/rate-limits

Verificar versões/disponibilidade gratuita/cotas no momento da implementação. Este guia não promete modelo específico gratuito para qualquer conta.
