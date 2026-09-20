# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 7 — Android Retrofit (integração parcial)  
**Branch:** `cursor/sprint2-backend-foundation-befe` (Android na mesma árvore)

---

## 1. Resumo etapa 7 (Android)

| Item | Estado |
|---|---|
| Retrofit + OkHttp + Moshi (BigDecimal monetário) | **Implementado** |
| DTOs / APIs / mappers REST (`api/v1`) | **Implementado** |
| Cinco repositórios migrados de Firebase → API | **Implementado** |
| TokenStore cifrado (EncryptedSharedPreferences + Keystore) | **Implementado** |
| Bearer + refresh (cliente separado, 1 retry) | **Implementado** |
| Dashboard líder via `/relatorios/dashboard` (sem cálculo local) | **Implementado** |
| Ranking via `/ranking` (sem Firestore) | **Implementado** |
| Sem gravação de pontos no app | **Implementado** |
| `BASE_URL` debug `http://10.0.2.2:8080/`; release HTTPS placeholder | **Implementado** |
| Cleartext só debug (`network_security_config`) | **Implementado** |
| Telas existentes ligadas aos repositórios REST | **Implementado** |
| Botão IA / telas novas (prompt 8) | **Não iniciado** (parar antes, conforme escopo) |
| `./gradlew :app:assembleDebug` | **OK** (agente Cloud) |
| `./gradlew :app:testDebugUnitTest` | **OK** (`MoneyMoshiTest`) |
| Execução em emulador/dispositivo | **Não executada** (sem SDK/emulador no agente) |

Firebase remoto **não alterado**; dependências/plugins Firebase removidos do módulo `app`; `google-services.json` permanece no repositório sem uso.

### Configuração API no dispositivo

- Emulador: `BuildConfig.API_BASE_URL` → `http://10.0.2.2:8080/`
- Aparelho físico: alterar `buildTypes.debug.buildConfigField` para o IP LAN do PC ou usar `adb reverse tcp:8080 tcp:8080` e `http://127.0.0.1:8080/`
- Backend deve estar em HTTP no dev (sem redirect para HTTPS inexistente)

---

## 2. Backend (etapas 2–6)

- Auth, estratégias, ideias, projetos, relatórios, ranking, IA Gemini — ver commits na branch `cursor/sprint2-backend-foundation-befe`.

---

## 3. Testes backend (agente Cloud)

```bash
cd backend && dotnet build -c Release && dotnet test ../tests/InovaGAB.IntegrationTests -c Release
```

Requer `MONGODB_URI` para integração.

---

## 4. Próxima etapa

**Prompt 8:** telas IA no Android, gráficos dashboard, polish UI — **não incluído nesta entrega**.
