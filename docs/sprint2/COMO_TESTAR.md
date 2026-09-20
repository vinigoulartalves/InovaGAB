# Como testar o InovaGAB Sprint 2 (checkout limpo)

Objetivo: validar a **API** e o **Android** sem Android Studio, a partir de scripts e `dotnet test` / `gradlew`.

**Não use banco de produção.** O Compose local usa o banco `inovagab` (dev). Testes automatizados usam bancos isolados (`inovagab_auth_test_*` por factory ou `inovagab_test_compose` no runner).

---

## Pré-requisitos

| Ferramenta | Versão fixada / referência |
|---|---|
| Docker + Compose | Para Mongo rs0 + test-runner |
| .NET SDK | `backend/global.json` → **8.0.425** |
| JDK | **17** (Android) |
| Bash | Linux/macOS/WSL |

Windows: use os scripts `*.ps1` equivalentes em `scripts/`.

---

## 1. Setup (uma vez)

```bash
git clone <repo> && cd InovaGAB
bash scripts/setup-dev.sh    # cria .env (não sobrescreve existente)
```

---

## 2. Subir API local (avaliação manual / Postman / app)

```bash
bash scripts/dev-up.sh         # compose: mongo + api, wait /health/ready
bash scripts/smoke-api.sh      # health + login operador1 (senhas do .env)
```

Base URL: `http://127.0.0.1:8080`

Usuários seed (quando `SEED_ENABLED=true`): `operador1@inovagab.local`, `operador2@inovagab.local`, `gestor@inovagab.local`, `lider@inovagab.local` — senhas em `.env` (`DEV_PASSWORD_*`).

---

## 3. Testes backend (recomendado — sem Android Studio)

### Via Docker (Mongo rs0 + test-runner na rede interna)

```bash
bash scripts/test-backend.sh
```

- Executa `scripts/test-runner-entrypoint.sh` dentro do SDK **8.0.425**
- **Exclui** `InovaGAB.IaExternalTests` do run padrão
- TRX/cobertura em `artifacts/test-results/` e `artifacts/coverage/` (ignorados no Git)

### No host (Mongo já rodando)

```bash
docker compose up -d mongo mongo-init
export MONGODB_URI='mongodb://127.0.0.1:27017/?replicaSet=rs0&directConnection=true'
bash scripts/test-backend-host.sh
```

### IA real (opt-in)

```bash
export AI_API_KEY='sua-chave'
bash scripts/test-ia-external.sh    # exit 2 se chave ausente (não verde falso)
```

### Reset de bancos de teste (manual, explícito)

```bash
export CONFIRM_RESET_TEST_DB=yes
bash scripts/reset-test-databases.sh
```

**Nunca** roda no startup. **Não** apaga o banco `inovagab` de desenvolvimento.

---

## 4. Android (sem IDE)

```bash
bash scripts/test-android.sh
```

- `:app:testDebugUnitTest` (ex.: `ApiMessagesTest`, `MoneyMoshiTest`)
- `:app:lintDebug`
- `:app:assembleDebug` → APK em `artifacts/android/`

### Dispositivo / emulador

| Item | Valor |
|---|---|
| Base URL debug | `http://10.0.2.2:8080/` (`BuildConfig.API_BASE_URL`) |
| Contas | Mesmas do seed `.env` / `operador1@inovagab.local` etc. |
| TestTags | `app/.../core/testing/TestTags.kt` |

**Instrumentado (API real):**

```bash
# Backend acessível do emulador; depois:
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.apiBaseUrl=http://10.0.2.2:8080/
```

`LoginSmokeInstrumentedTest` usa `Assume` se a API não responder (não passa verde sem backend).

**Roteiro manual por perfil:** ver seção 6 em `MATRIZ_TESTES.md`.

---

## 5. Postman

1. Importar `deliverables/postman/InovaGAB-Sprint2.postman_collection.json`
2. Duplicar `InovaGAB-Sprint2.postman_environment.template.json` → environment local
3. Preencher senhas do `.env` (não commitar)
4. Runner Newman (opcional): `newman run ... -e environment.json`

---

## 6. Evidências

```bash
bash scripts/collect-evidence.sh
```

Registra commit, versões de ferramentas e contagem de TRX em `artifacts/evidence-*.txt`.

---

## 7. CI (GitHub Actions)

Workflow: `.github/workflows/ci.yml` (actions/refs fixados).

**Importante:** a existência do YAML **não** garante que o pipeline já passou no repositório remoto; verifique a aba Actions após push.

IA real **não** roda no job padrão de PR.
