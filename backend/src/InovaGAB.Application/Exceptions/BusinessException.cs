namespace InovaGAB.Application.Exceptions;

public sealed class BusinessException : Exception
{
    public BusinessException(int statusCode, string code, string message)
        : base(message)
    {
        StatusCode = statusCode;
        Code = code;
    }

    public int StatusCode { get; }

    public string Code { get; }
}
