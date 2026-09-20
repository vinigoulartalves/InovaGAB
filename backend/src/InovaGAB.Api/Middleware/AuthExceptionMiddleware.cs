using System.Diagnostics;
using InovaGAB.Application.Exceptions;
using BusinessException = InovaGAB.Application.Exceptions.BusinessException;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Middleware;

public sealed class AuthExceptionMiddleware
{
    private readonly RequestDelegate _next;

    public AuthExceptionMiddleware(RequestDelegate next) => _next = next;

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (AuthException ex)
        {
            await WriteProblemAsync(context, ex.StatusCode, ex.Code, "Erro de autenticação", ex.Message);
        }
        catch (BusinessException ex)
        {
            await WriteProblemAsync(context, ex.StatusCode, ex.Code, "Erro de negócio", ex.Message);
        }
    }

    private static async Task WriteProblemAsync(
        HttpContext context,
        int status,
        string code,
        string title,
        string detail)
    {
        context.Response.StatusCode = status;
        context.Response.ContentType = "application/problem+json";

        var problem = new ProblemDetails
        {
            Title = title,
            Status = status,
            Detail = detail,
            Instance = context.Request.Path
        };
        problem.Extensions["traceId"] = Activity.Current?.Id ?? context.TraceIdentifier;
        problem.Extensions["code"] = code;

        await context.Response.WriteAsJsonAsync(problem);
    }
}
