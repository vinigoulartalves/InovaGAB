# Manifesto de entrega Sprint 2

| Campo | Valor |
|-------|-------|
| Data UTC | 2026-09-20T14:50:54Z |
| Git commit | 630215253d39bb77539dfb8a043a7ac55c80f540 |
| .NET SDK (host build) | 8.0.425 |
| Mongo (Compose) | mongo:7.0.24 |
| Modelo IA configurado | gemini-2.0-flash |

## Artefatos

| Arquivo | SHA-256 |
|---------|---------|
| InovaGAB_Backend_Sprint2.zip | 7859e200aa525085d43f3951f2fbacff70eb979655c3a670d38e02d7b9cda73e |
| InovaGAB_Android_Sprint2.zip | a632b0c1c69f71da1b2792bbfcaba116e19eb7b378fc95808e4d2965b896fb5e |
| app-debug.apk (fonte) | d1c9e0b05b1e20932248d2f773666a433c7b2ed6fe380c02fe6c1b097474daf9 |

## Roteiro de avaliação

1. Extrair Backend → `setup-dev.sh` → `dev-up.sh` → `smoke-api.sh`
2. Extrair Android → instalar `apk/app-debug.apk` com backend em `127.0.0.1:8080`
3. Opcional: Postman em `exemplos/postman/`
4. Apresentação: ver `docs/sprint2/APRESENTACAO.md` (PDF/PPT pendente)

## Exclusões do ZIP

.git, .env, segredos, bin/obj/build intermediários, local.properties; APK debug incluído por exceção.
