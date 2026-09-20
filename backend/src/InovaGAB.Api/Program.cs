using System.Text.Json.Serialization;
using InovaGAB.Api.Health;
using InovaGAB.Api.Middleware;
using InovaGAB.Application;
using InovaGAB.Infrastructure;
using InovaGAB.Infrastructure.Configuration;
using Microsoft.AspNetCore.Diagnostics.HealthChecks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

builder.Configuration.AddEnvironmentVariables();

BindOptions(builder);

builder.Services.AddProblemDetails(options =>
{
    options.CustomizeProblemDetails = context =>
    {
        context.ProblemDetails.Extensions["traceId"] =
            context.HttpContext.TraceIdentifier;
    };
});

builder.Services.AddControllers()
    .AddJsonOptions(json =>
    {
        json.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
        json.JsonSerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
    });

builder.Services.Configure<ApiBehaviorOptions>(options =>
{
    options.InvalidModelStateResponseFactory = context =>
    {
        var problem = new ValidationProblemDetails(context.ModelState)
        {
            Title = "Erro de validação",
            Status = StatusCodes.Status400BadRequest,
            Instance = context.HttpContext.Request.Path
        };
        problem.Extensions["traceId"] = context.HttpContext.TraceIdentifier;
        problem.Extensions["code"] = "VALIDACAO";
        return new BadRequestObjectResult(problem);
    };
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "InovaGAB API",
        Version = "v1",
        Description = "Sprint 2 — fundação. Contrato completo em docs/sprint2/openapi.yaml."
    });
});

builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);

builder.Services.AddHealthChecks()
    .AddCheck<MongoReadinessHealthCheck>("mongodb", tags: ["ready"]);

var app = builder.Build();

app.UseMiddleware<ExceptionHandlingMiddleware>();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.MapGet("/health/live", () => Results.Ok(new { status = "live" }))
    .AllowAnonymous()
    .WithName("HealthLive")
    .WithTags("Health");

app.MapHealthChecks("/health/ready", new HealthCheckOptions
{
    Predicate = check => check.Tags.Contains("ready"),
    ResponseWriter = async (context, report) =>
    {
        context.Response.ContentType = "application/json";
        var payload = new
        {
            status = report.Status.ToString(),
            checks = report.Entries.Select(e => new { name = e.Key, status = e.Value.Status.ToString() })
        };
        await context.Response.WriteAsJsonAsync(payload);
    }
}).AllowAnonymous();

app.MapControllers();

app.Run();

static void BindOptions(WebApplicationBuilder builder)
{
    builder.Services
        .AddOptions<MongoOptions>()
        .Bind(builder.Configuration.GetSection(MongoOptions.SectionName))
        .Validate(o => !string.IsNullOrWhiteSpace(o.ConnectionString), "Mongo:ConnectionString is required.")
        .ValidateOnStart();

    builder.Services
        .AddOptions<JwtOptions>()
        .Bind(builder.Configuration.GetSection(JwtOptions.SectionName))
        .Validate(o => !string.IsNullOrWhiteSpace(o.Secret), "Jwt:Secret is required.")
        .ValidateOnStart();

    builder.Services
        .AddOptions<AiOptions>()
        .Bind(builder.Configuration.GetSection(AiOptions.SectionName));

    builder.Services
        .AddOptions<SeedOptions>()
        .Bind(builder.Configuration.GetSection(SeedOptions.SectionName))
        .Validate(
            o => !builder.Environment.IsProduction() || !o.Enabled,
            "Seed must be disabled in Production.")
        .ValidateOnStart();
}

public partial class Program;
