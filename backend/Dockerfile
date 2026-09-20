# syntax=docker/dockerfile:1.7

FROM mcr.microsoft.com/dotnet/sdk:8.0.401-bookworm-slim AS build
WORKDIR /src
COPY backend/Directory.Build.props backend/Directory.Packages.props backend/global.json ./
COPY backend/src/InovaGAB.Domain/ ./src/InovaGAB.Domain/
COPY backend/src/InovaGAB.Application/ ./src/InovaGAB.Application/
COPY backend/src/InovaGAB.Infrastructure/ ./src/InovaGAB.Infrastructure/
COPY backend/src/InovaGAB.Api/ ./src/InovaGAB.Api/
RUN dotnet restore src/InovaGAB.Api/InovaGAB.Api.csproj
RUN dotnet publish src/InovaGAB.Api/InovaGAB.Api.csproj -c Release -o /app/publish /p:UseAppHost=false

FROM mcr.microsoft.com/dotnet/aspnet:8.0.21-bookworm-slim AS runtime
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN groupadd --system --gid 10001 appgroup \
    && useradd --system --uid 10001 --gid appgroup --home /app --shell /usr/sbin/nologin appuser \
    && chown -R appuser:appgroup /app
USER appuser
ENV ASPNETCORE_URLS=http://0.0.0.0:8080
EXPOSE 8080
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "InovaGAB.Api.dll"]
