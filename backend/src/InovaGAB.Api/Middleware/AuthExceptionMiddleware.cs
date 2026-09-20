using System.Diagnostics;
using InovaGAB.Application.Exceptions;
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
            context.Response.StatusCode = ex.StatusCode;
            context.Response.ContentType = "application/problem+json";

            var problem = new ProblemDetails
            {
                Title = "Erro de autenticação",
                Status = ex.StatusCode,
                Detail = ex.Message,
                Instance = context.Request.Path
            };
            problem.Extensions["traceId"] = Activity.Current?.Id ?? context.TraceIdentifier;
            problem.Extensions["code"] = ex.Code;

            await context.Response.WriteAsJsonAsync(problem);
        }
    }
}
