# Manifesto de entrega Sprint 2

| Campo | Valor |
|-------|-------|
| Data UTC | 2026-09-20T14:50:16Z |
| Git commit | 62955706afc53e8e31d9f5f6489cc53cb5459085 |
| .NET SDK (host build) | 8.0.425 |
| Mongo (Compose) | mongo:7.0.24 |
| Modelo IA configurado | gemini-2.0-flash |

## Artefatos

| Arquivo | SHA-256 |
|---------|---------|
| InovaGAB_Backend_Sprint2.zip | 6fa879c92a1dc88947bcab0fd0b71979d93d24b70573fdfe6dbc094229520392 |
| InovaGAB_Android_Sprint2.zip | 1826b3a9a4e110d7de2307cd179f574b04b7e320b4b34beabde593278f017594 |
| app-debug.apk (fonte) | d1c9e0b05b1e20932248d2f773666a433c7b2ed6fe380c02fe6c1b097474daf9 |

## Roteiro de avaliação

1. Extrair Backend → `setup-dev.sh` → `dev-up.sh` → `smoke-api.sh`
2. Extrair Android → instalar `apk/app-debug.apk` com backend em `127.0.0.1:8080`
3. Opcional: Postman em `exemplos/postman/`
4. Apresentação: ver `docs/sprint2/APRESENTACAO.md` (PDF/PPT pendente)

## Exclusões do ZIP

.git, .env, segredos, bin/obj/build intermediários, local.properties; APK debug incluído por exceção.
