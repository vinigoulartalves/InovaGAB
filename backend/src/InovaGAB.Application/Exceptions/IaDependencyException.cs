namespace InovaGAB.Application.Exceptions;

public sealed class IaDependencyException : Exception
{
    public IaDependencyException(int statusCode, string code, string message, Exception? inner = null)
        : base(message, inner)
    {
        StatusCode = statusCode;
        Code = code;
    }

    public int StatusCode { get; }

    public string Code { get; }
}
