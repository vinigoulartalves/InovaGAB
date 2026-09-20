namespace InovaGAB.Application.Exceptions;

public sealed class AuthException : Exception
{
    public AuthException(int statusCode, string code, string message)
        : base(message)
    {
        StatusCode = statusCode;
        Code = code;
    }

    public int StatusCode { get; }

    public string Code { get; }
}
