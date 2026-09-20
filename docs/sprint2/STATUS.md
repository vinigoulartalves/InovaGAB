# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 8 — Android funcionalidades API real (prompt 8)  
**Branch Android:** `cursor/sprint2-android-features-befe` (base: `cursor/sprint2-android-retrofit-befe`)

---

## 1. Resumo etapa 8 (Android)

| Item | Estado |
|---|---|
| Operador: estratégia vigente no formulário, editar/excluir ENVIADA, 409 amigável | **Implementado** |
| Líder: estratégias categoria/campanha/vigência/status, histórico, arquivadas visíveis | **Implementado** |
| Gestor: conversão ideia→projeto, selects API, IA na gestão, exclusão projeto | **Implementado** |
| Dashboard: filtros backend, gráficos Canvas, ROI null = Não aplicável | **Implementado** |
| API: PUT/DELETE ideias, conversão, analises-ia, histórico, dashboard query params | **Implementado** |
| DI: `AppViewModelFactory` + `inovaViewModel()` | **Implementado** |
| `TestTags` estáveis para automação | **Implementado** |
| `./gradlew :app:assembleDebug` | **OK** (agente Cloud) |
| `./gradlew :app:testDebugUnitTest` | **OK** |
| `./gradlew :app:connectedDebugAndroidTest` | **Não executado** (sem emulador/dispositivo conectado no agente) |
| Jornadas em emulador com evidência de vídeo | **Pendente** (ambiente sem AVD ativo) |

APK debug gerado: `app/build/outputs/apk/debug/app-debug.apk`

### Configuração API no dispositivo

- Emulador: `BuildConfig.API_BASE_URL` → `http://10.0.2.2:8080/`
- Backend local deve estar acessível (HTTP, sem redirect HTTPS)

---

## 2. Etapa 7 (Retrofit base)

Retrofit, TokenStore, repositórios REST, ranking, login — ver branch `cursor/sprint2-android-retrofit-befe` (PR #14).

---

## 3. Backend (etapas 2–6)

Auth, estratégias, ideias, projetos, relatórios, ranking, IA Gemini — branch `cursor/sprint2-backend-foundation-befe`.

---

## 4. Testes backend (agente Cloud)

```bash
cd backend && dotnet build -c Release && dotnet test ../tests/InovaGAB.IntegrationTests -c Release
```

Requer `MONGODB_URI` para integração.

---

## 5. Evidências IA backend

Ver `docs/sprint2/IA_EVIDENCIA.md` — chamada real Gemini **PENDENTE** de execução opt-in no ambiente com chave.
