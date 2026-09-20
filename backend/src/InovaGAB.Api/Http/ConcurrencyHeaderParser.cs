namespace InovaGAB.Api.Http;

public static class ConcurrencyHeaderParser
{
    public static bool TryParseIfMatch(string? headerValue, out int versao)
    {
        versao = 0;
        if (string.IsNullOrWhiteSpace(headerValue))
        {
            return false;
        }

        var trimmed = headerValue.Trim();
        if (trimmed.StartsWith("W/\"", StringComparison.Ordinal) && trimmed.EndsWith('"'))
        {
            trimmed = trimmed[3..^1];
        }
        else if (trimmed.StartsWith('"') && trimmed.EndsWith('"'))
        {
            trimmed = trimmed[1..^1];
        }

        return int.TryParse(trimmed, out versao);
    }
}
