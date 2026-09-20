using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Middleware;

public sealed class ExceptionHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionHandlingMiddleware> _logger;
    private readonly IHostEnvironment _environment;

    public ExceptionHandlingMiddleware(
        RequestDelegate next,
        ILogger<ExceptionHandlingMiddleware> logger,
        IHostEnvironment environment)
    {
        _next = next;
        _logger = logger;
        _environment = environment;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unhandled exception. TraceId={TraceId}", Activity.Current?.Id ?? context.TraceIdentifier);
            await WriteProblemAsync(context, StatusCodes.Status500InternalServerError, "ERRO_INTERNO", "Ocorreu um erro inesperado.");
        }
    }

    private static async Task WriteProblemAsync(HttpContext context, int status, string code, string detail)
    {
        if (context.Response.HasStarted)
        {
            return;
        }

        context.Response.StatusCode = status;
        context.Response.ContentType = "application/problem+json";

        var problem = new ProblemDetails
        {
            Title = "Erro na requisição",
            Status = status,
            Detail = detail,
            Instance = context.Request.Path
        };
        problem.Extensions["traceId"] = Activity.Current?.Id ?? context.TraceIdentifier;
        problem.Extensions["code"] = code;

        await context.Response.WriteAsJsonAsync(problem);
    }
}
