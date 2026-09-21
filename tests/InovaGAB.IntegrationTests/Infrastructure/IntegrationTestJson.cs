using System.Text.Json;
using System.Text.Json.Serialization;

namespace InovaGAB.IntegrationTests.Infrastructure;

public static class IntegrationTestJson
{
    public static JsonSerializerOptions Options { get; } = CreateOptions();

    private static JsonSerializerOptions CreateOptions()
    {
        var options = new JsonSerializerOptions(JsonSerializerDefaults.Web);
        options.Converters.Add(new JsonStringEnumConverter());
        return options;
    }
}
