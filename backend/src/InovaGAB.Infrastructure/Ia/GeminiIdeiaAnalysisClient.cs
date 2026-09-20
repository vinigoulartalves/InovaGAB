using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Nodes;
using InovaGAB.Application.Exceptions;
using InovaGAB.Application.Ideias;
using InovaGAB.Infrastructure.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace InovaGAB.Infrastructure.Ia;

public sealed class GeminiIdeiaAnalysisClient : IGeminiIdeiaAnalysisClient
{
    public const string HttpClientName = "GeminiIdeiaAnalysis";

    private readonly IHttpClientFactory _httpClientFactory;
    private readonly AiOptions _options;
    private readonly ILogger<GeminiIdeiaAnalysisClient> _logger;

    public GeminiIdeiaAnalysisClient(
        IHttpClientFactory httpClientFactory,
        IOptions<AiOptions> options,
        ILogger<GeminiIdeiaAnalysisClient> logger)
    {
        _httpClientFactory = httpClientFactory;
        _options = options.Value;
        _logger = logger;
    }

    public async Task<GeminiIdeiaAnalysisResult> AnalyzeAsync(
        GeminiIdeiaAnalysisRequest request,
        CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(_options.ApiKey))
        {
            throw new IaDependencyException(503, "IA_INDISPONIVEL", "Análise por IA não está configurada (chave ausente).");
        }

        if (!_options.Enabled)
        {
            throw new IaDependencyException(503, "IA_INDISPONIVEL", "Análise por IA está desabilitada.");
        }

        var model = string.IsNullOrWhiteSpace(request.Model) ? _options.Model : request.Model;
        var client = _httpClientFactory.CreateClient(HttpClientName);

        var schemaNode = JsonNode.Parse(request.ResponseJsonSchema)
            ?? throw new InvalidOperationException("Schema JSON inválido.");

        var body = new JsonObject
        {
            ["systemInstruction"] = new JsonObject
            {
                ["parts"] = new JsonArray
                {
                    new JsonObject { ["text"] = request.SystemInstruction }
                }
            },
            ["contents"] = new JsonArray
            {
                new JsonObject
                {
                    ["role"] = "user",
                    ["parts"] = new JsonArray
                    {
                        new JsonObject { ["text"] = request.UserPrompt }
                    }
                }
            },
            ["generationConfig"] = new JsonObject
            {
                ["temperature"] = 0.2,
                ["maxOutputTokens"] = request.MaxOutputTokens,
                ["responseMimeType"] = "application/json",
                ["responseSchema"] = schemaNode
            }
        };

        var url = $"v1beta/models/{model}:generateContent";
        using var httpRequest = new HttpRequestMessage(HttpMethod.Post, url);
        httpRequest.Headers.Add("x-goog-api-key", _options.ApiKey);
        httpRequest.Content = JsonContent.Create(body);

        try
        {
            using var response = await client.SendAsync(httpRequest, HttpCompletionOption.ResponseHeadersRead, cancellationToken);
            var responseText = await response.Content.ReadAsStringAsync(cancellationToken);

            if (response.StatusCode == HttpStatusCode.TooManyRequests)
            {
                throw new IaDependencyException(429, "IA_LIMITE", "Limite de requisições da IA excedido. Tente novamente mais tarde.");
            }

            if ((int)response.StatusCode >= 500)
            {
                _logger.LogWarning("Gemini retornou {StatusCode}", (int)response.StatusCode);
                throw new IaDependencyException(502, "IA_INDISPONIVEL", "Serviço de IA temporariamente indisponível.");
            }

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("Gemini retornou erro HTTP {StatusCode}", (int)response.StatusCode);
                throw new IaDependencyException(
                    503,
                    "IA_INDISPONIVEL",
                    "Não foi possível executar a análise (credencial inválida ou requisição rejeitada).");
            }

            var text = ExtractModelText(responseText);
            return new GeminiIdeiaAnalysisResult { RawJson = text };
        }
        catch (TaskCanceledException ex) when (!cancellationToken.IsCancellationRequested)
        {
            throw new IaDependencyException(504, "IA_TIMEOUT", "Tempo limite excedido ao consultar a IA.", ex);
        }
        catch (HttpRequestException ex)
        {
            throw new IaDependencyException(502, "IA_INDISPONIVEL", "Falha de rede ao consultar a IA.", ex);
        }
    }

    private static string ExtractModelText(string responseText)
    {
        using var doc = JsonDocument.Parse(responseText);
        var root = doc.RootElement;

        if (root.TryGetProperty("error", out var error))
        {
            var message = error.TryGetProperty("message", out var msg) ? msg.GetString() : "erro desconhecido";
            throw new IaDependencyException(502, "IA_INDISPONIVEL", $"IA retornou erro: {message}");
        }

        if (!root.TryGetProperty("candidates", out var candidates) || candidates.GetArrayLength() == 0)
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "Resposta da IA sem candidatos.");
        }

        var first = candidates[0];
        if (!first.TryGetProperty("content", out var content)
            || !content.TryGetProperty("parts", out var parts)
            || parts.GetArrayLength() == 0)
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "Resposta da IA sem conteúdo.");
        }

        var part0 = parts[0];
        if (!part0.TryGetProperty("text", out var textEl))
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "Resposta da IA sem texto JSON.");
        }

        var text = textEl.GetString();
        if (string.IsNullOrWhiteSpace(text))
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "JSON vazio na resposta da IA.");
        }

        return text;
    }
}
